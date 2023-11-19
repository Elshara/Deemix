import string
from deezer import TrackFormats
import os

USER_AGENT_HEADER = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " \
                    "Chrome/79.0.3945.130 Safari/537.36"

def canWrite(folder):
    return os.access(folder, os.W_OK)

def generateReplayGainString(trackGain):
    return "{0:.2f} dB".format((float(trackGain) + 18.4) * -1)

def getBitrateNumberFromText(txt):
    txt = str(txt).lower()
    if txt in ['flac', 'lossless', '9']:
        return TrackFormats.FLAC
    if txt in ['mp3', '320', '3']:
        return TrackFormats.MP3_320
    if txt in ['128', '1']:
        return TrackFormats.MP3_128
    if txt in ['360', '360_hq', '15']:
        return TrackFormats.MP4_RA3
    if txt in ['360_mq', '14']:
        return TrackFormats.MP4_RA2
    if txt in ['360_lq', '13']:
        return TrackFormats.MP4_RA1
    return None

def changeCase(txt, case_type):
    if case_type == "lower":
        return txt.lower()
    if case_type == "upper":
        return txt.upper()
    if case_type == "start":
        return string.capwords(txt)
    if case_type == "sentence":
        return txt.capitalize()
    return str

def removeFeatures(title):
    clean = title
    if "(feat." in clean.lower():
        pos = clean.lower().find("(feat.")
        tempTrack = clean[:pos]
        if ")" in clean:
            tempTrack += clean[clean.find(")", pos + 1) + 1:]
        clean = tempTrack.strip()
        clean = ' '.join(clean.split())
    return clean

def andCommaConcat(lst):
    tot = len(lst)
    result = ""
    for i, art in enumerate(lst):
        result += art
        if tot != i + 1:
            if tot - 1 == i + 1:
                result += " & "
            else:
                result += ", "
    return result

def uniqueArray(arr):
    for iPrinc, namePrinc  in enumerate(arr):
        for iRest, nRest in enumerate(arr):
            if iPrinc!=iRest and namePrinc.lower() in nRest.lower():
                del arr[iRest]
    return arr

def removeDuplicateArtists(artist, artists):
    artists = uniqueArray(artists)
    for role in artist.keys():
        artist[role] = uniqueArray(artist[role])
    return (artist, artists)
