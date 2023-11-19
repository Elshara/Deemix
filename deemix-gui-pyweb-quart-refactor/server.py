#!/usr/bin/env python3
import logging
import signal
import sys
from pathlib import Path
from os.path import sep as pathSep

import asyncio
import nest_asyncio
nest_asyncio.apply()

from functools import partial, wraps
from quart import Quart, render_template, session, request, redirect, websocket
from quart_cors import cors

from hypercorn.config import Config
from hypercorn.asyncio import serve

import secrets

from deemix import __version__ as deemix_version
from app import deemix, LoginStatus, resource_path
from deemix.app.messageinterface import MessageInterface

# Disable logging
logging.getLogger('quart.serving').setLevel(logging.ERROR)

# Setting up websocket broadcast
connected_websockets = set()

def collect_websocket(func):
    @wraps(func)
    async def wrapper(*args, **kwargs):
        global connected_websockets
        queue = asyncio.Queue()
        connected_websockets.add(queue)
        try:
            return await func(queue, *args, **kwargs)
        finally:
            connected_websockets.remove(queue)
    return wrapper

class SocketInterface(MessageInterface):
    def send(self, message, value=None):
        loop = asyncio.new_event_loop()
        for queue in connected_websockets:
            if value:
                data = {"key": message, "data": value}
            else:
                data = {"key": message}
            loop.run_until_complete(queue.put(data))

# Retrocompatibility with old versions of the app
# Check for public folder and fallback to webui
GUI_DIR = resource_path(f'webui{pathSep}public')
if not GUI_DIR.exists():
    GUI_DIR = resource_path('webui')
if not (GUI_DIR / 'index.html').is_file():
    sys.exit("WebUI not found, please download and add a WebUI")

# Setup server
server = Quart(__name__, static_folder=str(GUI_DIR), template_folder=str(GUI_DIR), static_url_path="")
server.config['SEND_FILE_MAX_AGE_DEFAULT'] = 1  # disable caching
server.secret_key = secrets.token_urlsafe(16)
server = cors(server)

app = None
gui = None
arl = None

socket_interface = SocketInterface()
first_connection = True
shutdown_event = asyncio.Event()

@server.route("/")
async def landing():
    return await render_template("index.html")

@server.route('/shutdown')
async def closing():
    shutdown()
    return {"result": True}

@server.route('/connect')
async def on_connect():
    result = {}

    if first_connection:
        app.checkForUpdates()
        app.checkDeezerAvailability()

    result["update"] = {
        'currentCommit': app.currentVersion,
        'latestCommit': app.latestVersion,
        'updateAvailable': app.updateAvailable,
        'deemixVersion': deemix_version
    }

    if arl:
        result["login"] = login(arl)
    else:
        result["autologin"] = True

    queue, queueComplete, queueList, currentItem = app.initDownloadQueue()
    if len(queueList.keys()):
        result["queueStatus"] = {
            'queue': queue,
            'queueComplete': queueComplete,
            'queueList': queueList,
            'currentItem': currentItem
        }

    if app.updateAvailable: result['updateAvailable'] = True
    if not app.isDeezerAvailable: result['deezerNotAvailable'] = True
    return result

@server.route('/api/getHome')
async def get_home_data():
    return app.get_home()

@server.route('/api/getCharts')
async def get_charts_data():
    return app.get_charts()

@server.route('/api/getSettings')
async def get_settings_data():
    return app.getAllSettings()

@server.route('/api/login')
async def login():
    global first_connection

    arl = request.args.get("arl")
    if not arl: return {'status': LoginStatus.FAILED}

    force = bool(request.args.get("force", False))
    child = request.args.get("child", 0)
    if child == "null": child = 0
    child = int(child)

    if not app.isDeezerAvailable:
        return {'status': LoginStatus.NOT_AVAILABLE, 'arl': arl, 'user': session['dz']['current_user']}

    arl = arl.strip()
    # emit('logging_in')

    if force and 'dz' in session: session.pop('dz')
    result = app.login(arl, int(child), session.get('dz'))
    session['dz'] = app.dz.get_session()
    if force and result == LoginStatus.SUCCESS: result = LoginStatus.FORCED_SUCCESS

    returnValue = {'status': result, 'arl': arl, 'user': session['dz']['current_user']}

    if first_connection and result in [LoginStatus.SUCCESS, LoginStatus.FORCED_SUCCESS]:
        first_connection = False
        app.restoreDownloadQueue(socket_interface)

    if result != 0:
        returnValue['childs'] = session['dz']['childs']
    return returnValue

@server.route('/api/changeAccount')
async def changeAccount():
    child = int(request.args.get("child", 0))
    if not child: return False
    app.dz.set_session(session['dz'])
    return app.dz.change_account(int(child))

@server.route('/api/logout')
async def logout():
    if session['dz']['logged_in']: session.pop('dz')
    return True

@server.route('/api/mainSearch')
async def mainSearch():
    term = request.args.get("term", "")
    if term.strip() != "":
        result = app.mainSearch(term)
        result['ack'] = request.args.get("ack")
        return result
    return []

@server.route('/api/search')
async def search():
    term = request.args.get("term", "")
    type = request.args.get("type", "")

    start = request.args.get("start", 0)
    if start == "null": start = 0
    start = int(start)
    nb = request.args.get("nb", 30)
    if nb == "null": nb = 30
    nb = int(nb)

    if term.strip() != "":
        result = app.search(term, type, start, nb)
        result['type'] = type
        result['ack'] = request.args.get('ack')
        return result
    return []

@server.route('/api/albumSearch')
async def albumSearch():
    term = request.args.get("term", "")

    start = request.args.get("start", 0)
    if start == "null": start = 0
    start = int(start)
    nb = request.args.get("nb", 30)
    if nb == "null": nb = 30
    nb = int(nb)

    if term.strip() != "":
        albums = app.searchAlbum(term, start, nb)
        output = {
            'data': albums,
            'total': len(albums),
            'ack': request.args.get('ack')
        };
        return output

@server.route('/api/newReleases')
async def newReleases():
    result = app.newReleases()
    output = {
        'data': result,
        'total': len(result),
        'ack': request.args.get('ack')
    };
    return output

@server.route('/api/addToQueue')
async def addToQueue():
    url = request.args.get("url")
    if not url: return False
    bitrate = request.args.get("bitrate")
    if bitrate == "null": bitrate = 3
    bitrate = int(bitrate)

    return app.addToQueue(session['dz'], url, bitrate, interface=socket_interface, ack=request.args.get('ack'))

@server.route('/api/removeFromQueue')
async def removeFromQueue():
    uuid = request.args.get("uuid")
    if not uuid: return False
    app.removeFromQueue(uuid, interface=socket_interface)
    return True

@server.route('/api/removeFinishedDownloads')
async def removeFinishedDownloads():
    app.removeFinishedDownloads(interface=socket_interface)
    return True

@server.route('/api/cancelAllDownloads')
async def cancelAllDownloads():
    app.cancelAllDownloads(interface=socket_interface)
    return True

#@server.route('/api/saveSettings')
#async def saveSettings(settings, spotifyCredentials, spotifyUser):
#    result = {"done": True}
#    app.saveSettings(settings, session['dz'])
#    app.setSpotifyCredentials(spotifyCredentials)
#    socket_interface.send('updateSettings', {"settings": settings, "spotifyCredentials": spotifyCredentials})
#    if spotifyUser != False:
#        result['shouldUpdateSpotifyPlaylists'] = True
#    return result

@server.route('/api/getTracklist')
async def getTracklist():
    type = request.args.get("type")
    id = request.args.get("id")
    if not (type or id): return {"error": "wrong_parameters"}

    return app.getTracklist(type, id)

@server.route('/api/analyzeLink')
async def analyzeLink():
    link = request.args.get("link")
    if not link: return {"error": "No link given"}

    (type, data) = app.analyzeLink(session['dz'], link)
    if len(data):
        return data
    else:
        return {"error": "Not Supported"}

@server.route('/api/getChartTracks')
async def getChartTracks():
    id = request.args.get("id")
    if not id: return False

    return {"result": app.dz.api.get_playlist_tracks(id)['data']}

@server.route('/api/getUserFavorites')
async def update_userFavorites():
    return app.getUserFavorites(session['dz'])

@server.route('/api/getUserSpotifyPlaylists')
async def update_userSpotifyPlaylists():
    spotifyUser = request.args.get("spotifyUser")
    if not spotifyUser: return {"error": "Missing argument: spotifyUser"}

    if spotifyUser != False:
        return app.updateUserSpotifyPlaylists(spotifyUser)
    return []

@server.route('/api/getUserPlaylists')
async def update_userPlaylists():
    return app.updateUserPlaylists(session['dz'])

@server.route('/api/getUserAlbums')
async def update_userAlbums():
    return app.updateUserAlbums(session['dz'])

@server.route('/api/getUserArtists')
async def update_userArtists():
    return app.updateUserArtists(session['dz'])

@server.route('/api/getUserTracks')
async def update_userTracks():
    return app.updateUserTracks(session['dz'])

@server.route('/openDownloadsFolder')
async def openDownloadsFolder():
    folder = app.getDownloadFolder()
    if sys.platform == 'darwin':
        subprocess.check_call(['open', folder])
    elif sys.platform == 'linux':
        subprocess.check_call(['xdg-open', folder])
    elif sys.platform == 'win32':
        subprocess.check_call(['explorer', folder])
    return True

#@server.route('/selectDownloadFolder')
#async def selectDownloadFolder():
#    if gui:
#        # Must be done with tpool to avoid blocking the greenthread
#        result = tpool.execute(doSelectDowloadFolder)
#        if result:
#            emit('downloadFolderSelected', result)
#    else:
#        print("Can't open folder selection, you're not running the gui")
#        return {"error": "Can't open folder selection, you're not running the gui"}
#
#def doSelectDowloadFolder():
#    gui.selectDownloadFolder_trigger.emit()
#    gui._selectDownloadFolder_semaphore.acquire()
#    return gui.downloadFolder
#
#@server.route('/applogin')
#def applogin():
#    if gui:
#        if not session['dz']['logged_in']:
#            # Must be done with tpool to avoid blocking the greenthread
#            arl = tpool.execute(dologin)
#            if arl:
#                emit('applogin_arl', arl)
#        else:
#            emit('logged_in', {'status': 2, 'user': session['dz']['current_user']})
#    else:
#        print("Can't open login page, you're not running the gui")
#
#def dologin():
#    gui.appLogin_trigger.emit()
#    gui._appLogin_semaphore.acquire()
#    return gui.arl

@server.websocket("/")
@collect_websocket
async def ws(queue):
    await websocket.accept()
    while True:
        data = await queue.get()
        await websocket.send_json(data)

@server.errorhandler(404)
async def not_found_handler(e):
    return redirect("/")

def shutdown():
    print("Shutting down server")
    if app is not None: app.shutdown(socket_interface)
    shutdown_event.set()

def run_server(host="127.0.0.1", port=6595, portable=None, guiWindow=None, server_arl=False):
    global app, gui, arl
    app = deemix(portable)
    gui = guiWindow
    if server_arl:
        print("Server-wide ARL enabled.")
        arl = app.getConfigArl()
    print("Starting server at http://" + host + ":" + str(port))
    try:
        config = Config()
        config.bind = [f"{host}:{port}"]
        try:
            loop = asyncio.get_event_loop()
            loop.add_signal_handler(signal.SIGINT,  shutdown)
            loop.add_signal_handler(signal.SIGTERM, shutdown)
            loop.run_until_complete(serve(server, config, shutdown_trigger=shutdown_event.wait))
        except RuntimeError:
            loop = asyncio.new_event_loop()
            asyncio.set_event_loop(loop)
            loop.run_until_complete(serve(server, config, shutdown_trigger=lambda: asyncio.Future()))
    except UnicodeDecodeError as e:
        print(str(e))
        print("A workaround for this issue is to remove all non roman characters from the computer name")
        print("More info here: https://bugs.python.org/issue26227")

if __name__ == '__main__':
    host = "127.0.0.1"
    port = 6595
    if len(sys.argv) >= 2:
        try: port = int(sys.argv[1])
        except ValueError: pass

    portable = None
    if '--portable' in sys.argv: portable = Path(__file__).parent / 'config'
    if '--host' in sys.argv:     host = str(sys.argv[sys.argv.index("--host")+1])
    serverwide_arl = "--serverwide-arl" in sys.argv

    run_server(host, port, portable, server_arl=serverwide_arl)
