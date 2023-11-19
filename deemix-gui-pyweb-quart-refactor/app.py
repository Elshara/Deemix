#!/usr/bin/env python3
import requests

from deezer import Deezer
from deezer.utils import clean_search_query
from deemix.app.settings import Settings, DEFAULT_SETTINGS
from deemix.app.queuemanager import QueueManager
from deemix.app.spotifyhelper import SpotifyHelper, emptyPlaylist as emptySpotifyPlaylist

from deemix.utils import getTypeFromLink, getIDFromLink
from deemix.utils.localpaths import getConfigFolder

from pathlib import Path
import json
import re

from datetime import datetime, timedelta

def resource_path(relative_path):
    """ Get absolute path to resource, works for dev and for PyInstaller """
    try:
        # PyInstaller creates a temp folder and stores path in _MEIPASS
        base_path = Path(sys._MEIPASS)
    except Exception:
        base_path = Path(__file__).resolve().parent

    return Path(base_path) / relative_path

class LoginStatus():
    """Login status codes"""

    NOT_AVAILABLE = -1
    """Deezer is not Available in your country"""

    FAILED = 0
    """Login Failed"""

    SUCCESS = 1
    """Login Successfull"""

    ALREADY_LOGGED = 2
    """Already logged in"""

    FORCED_SUCCESS = 3
    """Forced Login Successfull"""

class deemix:
    def __init__(self, portable):
        self.configFolder = portable or getConfigFolder()
        self.set = Settings(self.configFolder)
        self.dz = Deezer(accept_language = self.set.settings.get('tagsLanguage'))
        self.sp = SpotifyHelper(self.configFolder)
        self.qm = QueueManager(self.dz, self.sp)

        self.chartsList = []
        self.homeCache = None

        self.currentVersion = None
        self.latestVersion = None
        self.updateAvailable = False
        self.isDeezerAvailable = True

    def checkForUpdates(self):
        commitFile = resource_path('version.txt')
        if commitFile.is_file():
            with open(commitFile, 'r') as f:
                self.currentVersion = f.read().strip()
            #print("Checking for updates...")
            #try:
            #    latestVersion = requests.get("https://deemix.app/pyweb/latest")
            #    latestVersion.raise_for_status()
            #    self.latestVersion = latestVersion.text.strip()
            #except:
            #    self.latestVersion = None
            #self.updateAvailable = self.compareVersions()
            #if self.updateAvailable:
            #    print("Update available! Commit: "+self.latestVersion)
            #else:
            #    print("You're running the latest version")

    def compareVersions(self):
        if not self.latestVersion or not self.currentVersion:
            return False
        (currentDate, currentCommit) = tuple(self.currentVersion.split('-'))
        (latestDate, latestCommit) = tuple(self.latestVersion.split('-'))
        currentDate = currentDate.split('.')
        latestDate = latestDate.split('.')
        current = datetime(int(currentDate[0]), int(currentDate[1]), int(currentDate[2]))
        latest = datetime(int(latestDate[0]), int(latestDate[1]), int(latestDate[2]))
        if latest > current:
            return True
        elif latest == current:
            return latestCommit != currentCommit
        else:
             return False

    def checkDeezerAvailability(self):
        print("Pinging deezer.com...")
        try:
            body = requests.get("https://www.deezer.com/", headers={'Cookie': 'dz_lang=en; Domain=deezer.com; Path=/; Secure; hostOnly=false;'}).text
        except Exception as e:
            self.isDeezerAvailable = False
            print(f"deezer.com not reached! {str(e)}")
        title = body[body.find('<title>')+7:body.find('</title>')]
        self.isDeezerAvailable = title.strip() != "Deezer will soon be available in your country."
        print(f"deezer.com reached: {'Available' if self.isDeezerAvailable else 'Not Available'}")

    def shutdown(self, interface=None):
        if self.set.settings['saveDownloadQueue']: self.qm.saveQueue(self.configFolder)
        self.qm.cancelAllDownloads(interface)
        if interface: interface.send("toast", {'msg': "Server is closed."})
        if self.qm.queueThread: self.qm.queueThread.join()

    def getArl(self, tempDz):
        while True:
            arl = input("Paste here your arl: ")
            if not tempDz.login_via_arl(arl):
                print("ARL doesnt work. Mistyped or expired?")
            else:
                break
        with open(self.configFolder / '.arl', 'w') as f:
            f.write(arl)
        return arl

    def getConfigArl(self):
        tempDz = Deezer()
        arl = None
        if (self.configFolder / '.arl').is_file():
            with open(self.configFolder / '.arl', 'r') as f:
                arl = f.readline().rstrip("\n")
            if not tempDz.login_via_arl(arl):
                print("Saved ARL mistyped or expired, please enter a new one")
                return self.getArl(tempDz)
        else:
            return self.getArl(tempDz)
        return arl

    def login(self, arl, child, dzSession=None):
        if dzSession: self.dz.set_session(dzSession)
        if not self.dz.logged_in:
            return int(self.dz.login_via_arl(arl, child))
        else:
            return LoginStatus.ALREADY_LOGGED

    def restoreDownloadQueue(self, interface=None):
        self.qm.loadQueue(self.configFolder, self.set.settings, interface)
        self.qm.startQueue()

    def get_charts(self):
        if len(self.chartsList) == 0:
            temp = self.dz.api.get_countries_charts()
            countries = []
            for i in range(len(temp)):
                countries.append({
                    'title': temp[i]['title'].replace("Top ", ""),
                    'id': temp[i]['id'],
                    'picture_small': temp[i]['picture_small'],
                    'picture_medium': temp[i]['picture_medium'],
                    'picture_big': temp[i]['picture_big']
                })
            self.chartsList = countries
        return {"data": self.chartsList}

    def get_home(self):
        if not self.homeCache:
            self.homeCache = self.dz.api.get_chart(limit=30)
        return self.homeCache

    def getDownloadFolder(self):
        return self.set.settings['downloadLocation']

    def getTracklist(self, type, id):
        if type == 'artist':
            artistAPI = self.dz.api.get_artist(id)
            artistAPI['releases'] = self.dz.gw.get_artist_discography_tabs(id, 100)
            return artistAPI
        elif type == 'spotifyplaylist':
            playlistAPI = self.getSpotifyPlaylistTracklist(id)
            for i in range(len(playlistAPI['tracks'])):
                playlistAPI['tracks'][i] = playlistAPI['tracks'][i]['track']
                playlistAPI['tracks'][i]['selected'] = False
            return playlistAPI
        else:
            releaseAPI = getattr(self.dz.api, 'get_' + type)(id)
            releaseTracksAPI = getattr(self.dz.api, 'get_' + type + '_tracks')(id)['data']
            tracks = []
            showdiscs = False
            if type == 'album' and len(releaseTracksAPI) and releaseTracksAPI[-1]['disk_number'] != 1:
                current_disk = 0
                showdiscs = True
            for track in releaseTracksAPI:
                if showdiscs and int(track['disk_number']) != current_disk:
                    current_disk = int(track['disk_number'])
                    tracks.append({'type': 'disc_separator', 'number': current_disk})
                track['selected'] = False
                tracks.append(track)
            releaseAPI['tracks'] = tracks
            return releaseAPI

    def getUserFavorites(self, dzSession):
        self.dz.set_session(dzSession)
        result = {}
        if self.dz.logged_in:
            user_id = self.dz.current_user['id']
            try:
                result['playlists'] = self.dz.api.get_user_playlists(user_id, limit=-1)['data']
                result['albums'] = self.dz.api.get_user_albums(user_id, limit=-1)['data']
                result['artists'] = self.dz.api.get_user_artists(user_id, limit=-1)['data']
                result['tracks'] = self.dz.api.get_user_tracks(user_id, limit=-1)['data']
            except:
                result['playlists'] = self.dz.gw.get_user_playlists(user_id, limit=-1)
                result['albums'] = self.dz.gw.get_user_albums(user_id, limit=-1)
                result['artists'] = self.dz.gw.get_user_artists(user_id, limit=-1)
                result['tracks'] = self.dz.gw.get_user_tracks(user_id, limit=-1)
        return result

    def updateUserSpotifyPlaylists(self, user):
        if user == "" or not self.sp.spotifyEnabled:
            return {"data": []}
        try:
            return {"data": self.sp.get_user_playlists(user)}
        except:
            return {"data": []}

    def updateUserPlaylists(self, dzSession):
        self.dz.set_session(dzSession)
        if self.dz.logged_in:
            user_id = self.dz.current_user['id']
            try:
                return self.dz.api.get_user_playlists(user_id, limit=-1)['data']
            except:
                return self.dz.gw.get_user_playlists(user_id, limit=-1)
        return {"error": "notLoggedIn"}

    def updateUserAlbums(self, dzSession):
        self.dz.set_session(dzSession)
        if self.dz.logged_in:
            user_id = self.dz.current_user['id']
            try:
                return self.dz.api.get_user_albums(user_id, limit=-1)['data']
            except:
                return self.dz.gw.get_user_albums(user_id, limit=-1)
        return {"error": "notLoggedIn"}

    def updateUserArtists(self, dzSession):
        self.dz.set_session(dzSession)
        if self.dz.logged_in:
            user_id = self.dz.current_user['id']
            try:
                return self.dz.api.get_user_artists(user_id, limit=-1)['data']
            except:
                return self.dz.gw.get_user_artists(user_id, limit=-1)
        return {"error": "notLoggedIn"}

    def updateUserTracks(self, dzSession):
        self.dz.set_session(dzSession)
        if self.dz.logged_in:
            user_id = self.dz.current_user['id']
            try:
                return self.dz.api.get_user_tracks(user_id, limit=-1)['data']
            except:
                return self.dz.gw.get_user_tracks(user_id, limit=-1)
        return {"error": "notLoggedIn"}

    def getSpotifyPlaylistTracklist(self, id):
        if id == "" or not self.sp.spotifyEnabled:
            return emptySpotifyPlaylist
        return self.sp.get_playlist_tracklist(id)

    # Search functions
    def mainSearch(self, term):
        results = self.dz.gw.search(clean_search_query(term))
        order = []
        for x in results['ORDER']:
            if x in ['TOP_RESULT', 'TRACK', 'ALBUM', 'ARTIST', 'PLAYLIST']:
                order.append(x)
        if 'TOP_RESULT' in results and len(results['TOP_RESULT']):
            orig_top_result = results['TOP_RESULT'][0]
            top_result = {}
            top_result['type'] = orig_top_result['__TYPE__']
            if top_result['type'] == 'artist':
                top_result['id'] = orig_top_result['ART_ID']
                top_result['picture'] = 'https://e-cdns-images.dzcdn.net/images/artist/' + orig_top_result['ART_PICTURE']
                top_result['title'] = orig_top_result['ART_NAME']
                top_result['nb_fan'] = orig_top_result['NB_FAN']
            elif top_result['type'] == 'album':
                top_result['id'] = orig_top_result['ALB_ID']
                top_result['picture'] = 'https://e-cdns-images.dzcdn.net/images/cover/' + orig_top_result['ALB_PICTURE']
                top_result['title'] = orig_top_result['ALB_TITLE']
                top_result['artist'] = orig_top_result['ART_NAME']
                top_result['nb_song'] = orig_top_result['NUMBER_TRACK']
            elif top_result['type'] == 'playlist':
                top_result['id'] = orig_top_result['PLAYLIST_ID']
                top_result['picture'] = 'https://e-cdns-images.dzcdn.net/images/' + orig_top_result['PICTURE_TYPE'] + '/' + orig_top_result['PLAYLIST_PICTURE']
                top_result['title'] = orig_top_result['TITLE']
                top_result['artist'] = orig_top_result['PARENT_USERNAME']
                top_result['nb_song'] = orig_top_result['NB_SONG']
            else:
                top_result['id'] = "0"
                top_result['picture'] = 'https://e-cdns-images.dzcdn.net/images/cover'
            top_result['picture'] += '/156x156-000000-80-0-0.jpg'
            top_result['link'] = 'https://deezer.com/'+top_result['type']+'/'+str(top_result['id'])
            results['TOP_RESULT'][0] = top_result
        results['ORDER'] = order
        return results

    def search(self, term, type, start, nb):
        if type == "album":
            return self.dz.api.search_album(clean_search_query(term), limit=nb, index=start)
        if type == "artist":
            return self.dz.api.search_artist(clean_search_query(term), limit=nb, index=start)
        if type == "playlist":
            return self.dz.api.search_playlist(clean_search_query(term), limit=nb, index=start)
        if type == "radio":
            return self.dz.api.search_radio(clean_search_query(term), limit=nb, index=start)
        if type == "track":
            return self.dz.api.search_track(clean_search_query(term), limit=nb, index=start)
        if type == "user":
            return self.dz.api.search_user(clean_search_query(term), limit=nb, index=start)
        return self.dz.api.search(clean_search_query(term), limit=nb, index=start)

    def getAlbumDetails(self, album_id):
        result = self.dz.gw.get_album_page(album_id)
        output = result['DATA']

        duration = 0
        for x in result['SONGS']['data']:
            try:
                duration += int(x['DURATION'])
            except:
                pass

        output['DURATION'] = duration
        output['NUMBER_TRACK'] = result['SONGS']['total']
        output['LINK'] = f"https://deezer.com/album/{str(output['ALB_ID'])}"

        return output

    def searchAlbum(self, term, start, nb):
        results = self.dz.gw.search_music(clean_search_query(term), "ALBUM", start, nb)['data']

        ids = [x['ALB_ID'] for x in results]

        def albumDetailsWorker(album_id):
            return self.getAlbumDetails(album_id)
        pool = eventlet.GreenPool(100)
        albums = [a for a in pool.imap(albumDetailsWorker, ids)]

        return albums

    def channelNewReleases(self, channel_name):
        channel_data = self.dz.gw.get_page(channel_name)
        pattern = '^New.*releases$'
        new_releases = next((x for x in channel_data['sections'] if re.match(pattern, x['title'])), None)

        try:
            if new_releases is None:
                return []
            elif 'target' in new_releases:
                show_all = self.dz.gw.get_page(new_releases['target'])
                return [x['data'] for x in show_all['sections'][0]['items']]
            elif 'items' in new_releases:
                return [x['data'] for x in new_releases['items']]
            else:
                return []
        except Exception:
            return []

    def newReleases(self):
        explore = self.dz.gw.get_page('channels/explore')
        music_section = next((x for x in explore['sections'] if x['title'] == 'Music'), None)
        channels = [x['target'] for x in music_section['items']]

        def channelWorker(channel):
            return self.channelNewReleases(channel)
        pool = eventlet.GreenPool(100)
        new_releases_lists = [x for x in pool.imap(channelWorker, channels[1:10])]

        seen = set()
        new_releases = [seen.add(x['ALB_ID']) or x for list in new_releases_lists for x in list if x['ALB_ID'] not in seen]
        new_releases.sort(key=lambda x: x['DIGITAL_RELEASE_DATE'], reverse=True)

        now = datetime.now()
        delta = timedelta(days=8)
        recent_releases = [x for x in new_releases if now - datetime.strptime(x['DIGITAL_RELEASE_DATE'], "%Y-%m-%d") < delta]
        recent_releases.sort(key=lambda x: x['ALB_ID'], reverse=True)

        def albumDetailsWorker(album_id):
            return self.getAlbumDetails(album_id)
        albums = [a for a in pool.imap(albumDetailsWorker, [x['ALB_ID'] for x in recent_releases])]

        return albums

    # Queue functions
    def addToQueue(self, dzSession, url, bitrate=None, interface=None, ack=None):
        self.dz.set_session(dzSession)
        if ';' in url: url = url.split(";")
        result = self.qm.addToQueue(url, self.set.settings, bitrate, interface=interface, ack=ack)
        return {"result": result}

    def removeFromQueue(self, uuid, interface=None):
        result = self.qm.removeFromQueue(uuid, interface)
        return {"result": result}

    def cancelAllDownloads(self, interface=None):
        result = self.qm.cancelAllDownloads(interface)
        return {"result": result}

    def removeFinishedDownloads(self, interface=None):
        result = self.qm.removeFinishedDownloads(interface)
        return {"result": result}

    def initDownloadQueue(self):
        (queue, queueComplete, queueList, currentItem) = self.qm.getQueue()
        return (queue, queueComplete, queueList, currentItem)

    def analyzeLink(self, link):
        if 'deezer.page.link' in link:
            link = requests.get(link).url
        type = getTypeFromLink(link)
        relID = getIDFromLink(link, type)
        if type in ["track", "album"]:
            data = getattr(self.dz.api, 'get_' + type)(relID)
        else:
            data = {}
        return (type, data)

    # Settings functions
    def getAllSettings(self):
        return (self.set.settings, self.sp.getCredentials(), DEFAULT_SETTINGS)

    def getDefaultSettings(self):
        return DEFAULT_SETTINGS

    def getSettings(self):
        return self.set.settings

    def saveSettings(self, newSettings, dzSession=None):
        if dzSession: self.dz.set_session(dzSession)
        return self.set.saveSettings(newSettings, self.dz)

    def getSpotifyCredentials(self):
        return self.sp.getCredentials()

    def setSpotifyCredentials(self, newCredentials):
        return self.sp.setCredentials(newCredentials)
