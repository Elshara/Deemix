// Starting area, boot up the API and proceed to eat memory

// Variables & constants
const socket = io.connect(window.location.href)
const serverMode = typeof require === "undefined"
var defaultUserSettings = {}
const localStorage = window.localStorage
var modalQuality = document.getElementById('modal_quality');
modalQuality.open = false
let userSettings = {}
let spotifySettings = {}
var currentSearch = ""
var searchData = {
	track: {
		next: 0,
		total: 0
	},
	album: {
		next: 0,
		total: 0
	},
	artist: {
		next: 0,
		total: 0
	},
	playlist: {
		next: 0,
		total: 0
	}
}

var queue = []
var queueList = {}
var queueComplete = []
var loggedIn = false
var deezerNotAvailable = false

let preview_track = document.getElementById('preview-track')
let preview_stopped = true
let preview_max_volume;
const COUNTRIES = {"AF": "Afghanistan","AX": "\u00c5land Islands","AL": "Albania","DZ": "Algeria","AS": "American Samoa","AD": "Andorra","AO": "Angola","AI": "Anguilla","AQ": "Antarctica","AG": "Antigua and Barbuda","AR": "Argentina","AM": "Armenia","AW": "Aruba","AU": "Australia","AT": "Austria","AZ": "Azerbaijan","BS": "Bahamas","BH": "Bahrain","BD": "Bangladesh","BB": "Barbados","BY": "Belarus","BE": "Belgium","BZ": "Belize","BJ": "Benin","BM": "Bermuda","BT": "Bhutan","BO": "Bolivia, Plurinational State of","BQ": "Bonaire, Sint Eustatius and Saba","BA": "Bosnia and Herzegovina","BW": "Botswana","BV": "Bouvet Island","BR": "Brazil","IO": "British Indian Ocean Territory","BN": "Brunei Darussalam","BG": "Bulgaria","BF": "Burkina Faso","BI": "Burundi","KH": "Cambodia","CM": "Cameroon","CA": "Canada","CV": "Cape Verde","KY": "Cayman Islands","CF": "Central African Republic","TD": "Chad","CL": "Chile","CN": "China","CX": "Christmas Island","CC": "Cocos (Keeling) Islands","CO": "Colombia","KM": "Comoros","CG": "Congo","CD": "Congo, the Democratic Republic of the","CK": "Cook Islands","CR": "Costa Rica","CI": "C\u00f4te d'Ivoire","HR": "Croatia","CU": "Cuba","CW": "Cura\u00e7ao","CY": "Cyprus","CZ": "Czech Republic","DK": "Denmark","DJ": "Djibouti","DM": "Dominica","DO": "Dominican Republic","EC": "Ecuador","EG": "Egypt","SV": "El Salvador","GQ": "Equatorial Guinea","ER": "Eritrea","EE": "Estonia","ET": "Ethiopia","FK": "Falkland Islands (Malvinas)","FO": "Faroe Islands","FJ": "Fiji","FI": "Finland","FR": "France","GF": "French Guiana","PF": "French Polynesia","TF": "French Southern Territories","GA": "Gabon","GM": "Gambia","GE": "Georgia","DE": "Germany","GH": "Ghana","GI": "Gibraltar","GR": "Greece","GL": "Greenland","GD": "Grenada","GP": "Guadeloupe","GU": "Guam","GT": "Guatemala","GG": "Guernsey","GN": "Guinea","GW": "Guinea-Bissau","GY": "Guyana","HT": "Haiti","HM": "Heard Island and McDonald Islands","VA": "Holy See (Vatican City State)","HN": "Honduras","HK": "Hong Kong","HU": "Hungary","IS": "Iceland","IN": "India","ID": "Indonesia","IR": "Iran, Islamic Republic of","IQ": "Iraq","IE": "Ireland","IM": "Isle of Man","IL": "Israel","IT": "Italy","JM": "Jamaica","JP": "Japan","JE": "Jersey","JO": "Jordan","KZ": "Kazakhstan","KE": "Kenya","KI": "Kiribati","KP": "Korea, Democratic People's Republic of","KR": "Korea, Republic of","KW": "Kuwait","KG": "Kyrgyzstan","LA": "Lao People's Democratic Republic","LV": "Latvia","LB": "Lebanon","LS": "Lesotho","LR": "Liberia","LY": "Libya","LI": "Liechtenstein","LT": "Lithuania","LU": "Luxembourg","MO": "Macao","MK": "Macedonia, the Former Yugoslav Republic of","MG": "Madagascar","MW": "Malawi","MY": "Malaysia","MV": "Maldives","ML": "Mali","MT": "Malta","MH": "Marshall Islands","MQ": "Martinique","MR": "Mauritania","MU": "Mauritius","YT": "Mayotte","MX": "Mexico","FM": "Micronesia, Federated States of","MD": "Moldova, Republic of","MC": "Monaco","MN": "Mongolia","ME": "Montenegro","MS": "Montserrat","MA": "Morocco","MZ": "Mozambique","MM": "Myanmar","NA": "Namibia","NR": "Nauru","NP": "Nepal","NL": "Netherlands","NC": "New Caledonia","NZ": "New Zealand","NI": "Nicaragua","NE": "Niger","NG": "Nigeria","NU": "Niue","NF": "Norfolk Island","MP": "Northern Mariana Islands","NO": "Norway","OM": "Oman","PK": "Pakistan","PW": "Palau","PS": "Palestine, State of","PA": "Panama","PG": "Papua New Guinea","PY": "Paraguay","PE": "Peru","PH": "Philippines","PN": "Pitcairn","PL": "Poland","PT": "Portugal","PR": "Puerto Rico","QA": "Qatar","RE": "R\u00e9union","RO": "Romania","RU": "Russian Federation","RW": "Rwanda","BL": "Saint Barth\u00e9lemy","SH": "Saint Helena, Ascension and Tristan da Cunha","KN": "Saint Kitts and Nevis","LC": "Saint Lucia","MF": "Saint Martin (French part)","PM": "Saint Pierre and Miquelon","VC": "Saint Vincent and the Grenadines","WS": "Samoa","SM": "San Marino","ST": "Sao Tome and Principe","SA": "Saudi Arabia","SN": "Senegal","RS": "Serbia","SC": "Seychelles","SL": "Sierra Leone","SG": "Singapore","SX": "Sint Maarten (Dutch part)","SK": "Slovakia","SI": "Slovenia","SB": "Solomon Islands","SO": "Somalia","ZA": "South Africa","GS": "South Georgia and the South Sandwich Islands","SS": "South Sudan","ES": "Spain","LK": "Sri Lanka","SD": "Sudan","SR": "Suriname","SJ": "Svalbard and Jan Mayen","SZ": "Swaziland","SE": "Sweden","CH": "Switzerland","SY": "Syrian Arab Republic","TW": "Taiwan, Province of China","TJ": "Tajikistan","TZ": "Tanzania, United Republic of","TH": "Thailand","TL": "Timor-Leste","TG": "Togo","TK": "Tokelau","TO": "Tonga","TT": "Trinidad and Tobago","TN": "Tunisia","TR": "Turkey","TM": "Turkmenistan","TC": "Turks and Caicos Islands","TV": "Tuvalu","UG": "Uganda","UA": "Ukraine","AE": "United Arab Emirates","GB": "United Kingdom","US": "United States","UM": "United States Minor Outlying Islands","UY": "Uruguay","UZ": "Uzbekistan","VU": "Vanuatu","VE": "Venezuela, Bolivarian Republic of","VN": "Viet Nam","VG": "Virgin Islands, British","VI": "Virgin Islands, U.S.","WF": "Wallis and Futuna","EH": "Western Sahara","YE": "Yemen","ZM": "Zambia","ZW": "Zimbabwe"}

function toast(message, icon){
	if (icon){
		message = '<i class="material-icons left">'+icon+'</i>'+message
	}
	M.toast({html: message, displayLength: 5000, classes: 'rounded'})
}

window.addEventListener('offline', function(e) {
	toast("You are offline!", 'warning')
});
window.addEventListener('online', function(e) {
	toast("Back online!", 'check')
	if (!loggedIn) checkAutologin();
});

socket.on("toast", function(data){
	toast(data['msg'], 'error')
})

/*socket.on("deezerNotAvailable", function(){
	$("#deezerNotAvailable").slideDown()
	deezerNotAvailable = true
})*/

// Prints object obj into console
// For Debug purposes
socket.on("printObj", function(obj){
	console.log(obj)
})

socket.on("init_update", function(data){
	if (data.currentCommit)
		$('#application_version_about').text(data.currentCommit)
	else
		$('#application_version_about').text("N/A")
})

// Update ARL button
$('#modal_settings_btn_updateArl').click(function () {
	$('#modal_settings_btn_updateArl').attr("disabled", true)
	var savedArl = localStorage.getItem('arl')
	var currentArl = $('#modal_login_input_arl').val()
	if (savedArl != currentArl){
		if (navigator.onLine){
			socket.emit('login', currentArl, true)
		}else{
			toast("You are offline!", 'warning')
		}
	}
	$('#modal_settings_btn_updateArl').attr("disabled", false)
})

$("#modal_settings_btn_copyArl").click(function(){
	$("#modal_login_input_arl").attr("type", "text");
	document.querySelector("#modal_login_input_arl").select();
	document.execCommand("copy");
	$("#modal_login_input_arl").attr("type", "password");
	toast("ARL copied to clipboard", 'assignment')
})

// After Login
socket.on("logged_in", function (data) {
	if (data.status != 0) {
		$("#modal_settings_username").text(data.user.name)
		$("#modal_settings_picture").attr("src", `https://e-cdns-images.dzcdn.net/images/user/${data.user.picture}/128x128-000000-80-0-0.jpg`)
		$("#side_user").text(data.user.name)
		$("#side_avatar").attr("src", `https://e-cdns-images.dzcdn.net/images/user/${data.user.picture}/128x128-000000-80-0-0.jpg`)
		$("#side_email").text("id:"+data.user.id)
		if (data.user.id != 0){
			localStorage.setItem('arl', data.arl)
			$("#modal_login_input_arl").val(data.arl)
			// Load personal public playlists
			//socket.emit("getMyPlaylistList", {spotifyUser: localStorage.getItem('spotifyUser')})
			$('#logged_in_info').removeClass('hide')
			$('#login_email_btn_container').addClass('hide')
			//$('#modal_login').modal("close")
			//$('#modal_login_input_password').val("")
			toast("Logged in successfully", 'check')
			loggedIn = true;
		}
	}else{
		if (deezerNotAvailable) data.error = "Error: "+"Deezer is not available in your country"
		$('#login-res-text').text(data.error)
		setTimeout(function(){$('#login-res-text').text("")},3000)
		toast(data.error, 'error')
		$('#modal_login_input_arl').val("")
		if (localStorage.getItem("arl")){
			localStorage.removeItem("arl")
		}
		loggedIn = false;
	}
	M.updateTextFields()
})

// Autologin
function checkAutologin(){
	if (navigator.onLine){
		if (localStorage.getItem('arl')){
			toast("Attempting Autologin...", 'info')
			socket.emit('login', localStorage.getItem('arl'))
			$('#modal_login_input_arl').val(localStorage.getItem('arl'))
			M.updateTextFields()
		}
	}else{
		if (localStorage.getItem('arl'))
			$('#modal_login_input_arl').val(localStorage.getItem('arl'))
		toast("You are offline!", 'warning')
		loggedIn = false;
	}
}
socket.on('init_autologin', function(){checkAutologin()})

// Logout Button
$('#modal_settings_btn_logout').click(function () {
	$('#modal_login_input_arl').val("")
	$('#logged_in_info').addClass('hide')
	localStorage.removeItem("arl")
	socket.emit('logout')
	loggedIn = false;
	M.updateTextFields()
})

// Open downloads folder
$('#openDownloadsFolder').on('click', function () {
	socket.emit('openDownloadsFolder')
})

// Alert for replayGain tag
$('#modal_tags_replayGain').on('click', function() {
	if ($(this).is(':checked')) {
		message("Warning", "Saving replay gain causes tracks to be quieter for some users.")
	}
})

// Do misc stuff on page load
$(document).ready(function () {
	// Page Initializing
	console.log("Document ready")
	$("main.container").css('display', 'block')
	M.AutoInit()

	// Track Preview stuff
	preview_track.volume = 0
	preview_max_volume = parseInt(localStorage.getItem("previewVolume"))
	if (localStorage.getItem("previewVolume") === null){
		preview_max_volume = 80
		localStorage.setItem("previewVolume", preview_max_volume)
	}
	$('#modal_settings_range_previewVolume').val(preview_max_volume)

	// Init tabs and modals
	var tabs = M.Tabs.getInstance(document.getElementById("tab-nav"))
	$('.modal').modal()
	$("main.container").addClass('animated fadeIn').on('webkitAnimationEnd', function () {
		$(this).removeClass('animated fadeOut')
	})

	// Continuous scrolling search
	$(window).scroll(function () {
    if ($(document).height() <= $(window).scrollTop() + $(window).height()) {
      if (tabs.index == 0){
				var mode = $('#tab_search_form_search').find('input[name=searchMode]:checked').val()
				if (searchData[mode].next != searchData[mode].total){
					searchString = currentSearch

					if (searchString.length == 0) {return}
					$('#tab_search_table_results_tbody_loadingIndicator').removeClass('hide')
					socket.emit("search", {type: mode, term: searchString, start: searchData[mode].next, nb: 30})
				}
			}
    }
  });

	// Change Preview Volume
	$('#modal_settings_range_previewVolume').on('change',function(event){
		preview_max_volume = $(this).val()
		localStorage.setItem("previewVolume", preview_max_volume)
	})

	// Illegal character settings
	$("#modal_settings_input_illegalCharacterReplacer").keypress( function(e) {
		const regex = RegExp('[\0\/\\:*?"<>|]');
		if (regex.test(e.key) && e.key != 'backspace') {
				e.preventDefault();
		}
	});

	// Load top charts list for countries
	if (localStorage.getItem('chart') == null)
		localStorage.setItem('chart', "Worldwide")

	// Side Nav Stuff
	$('.sidenav').sidenav({
		edge: 'right',
		draggable: true
	})

	$('.sidenav_tab').click((e)=>{
		e.preventDefault()
		$(e.currentTarget).addClass("active")
		tabs.select($(e.currentTarget).attr('tab-id'))
		tabs.updateTabIndicator()
	})

	// scrollToTop FAB
	$(window).scroll(function () {
		if ($(this).scrollTop() > 100) {
			$('#btn_scrollToTop a').removeClass('scale-out').addClass('scale-in')
		} else {
			$('#btn_scrollToTop a').removeClass('scale-in').addClass('scale-out')
		}
	})

	$('#btn_scrollToTop').click(function () {
		$('html, body').animate({scrollTop: 0}, 800)
		return false
	})

	// Playlist Stuff
	$("#button_refresh_playlist_tab").click(function(){
		$("table_personal_playlists").html("")
		socket.emit("update_userFavorites")
		if (localStorage.getItem('spotifyUser'))
			socket.emit('update_userSpotifyPlaylists', localStorage.getItem('spotifyUser'))
	})

	$('#downloadChartPlaylist').on('contextmenu', function(e){
    e.preventDefault();
		$(modalQuality).data("url", `https://www.deezer.com/playlist/${$(this).data("id")}`)
		$(modalQuality).css('display', 'block')
		$(modalQuality).addClass('animated fadeIn')
    return false;
	}).on('click', function(e){
    e.preventDefault();
    sendAddToQueue(`https://www.deezer.com/playlist/${$(this).data("id")}`)
	})

	// Track Preview Feature
	$(preview_track).on('canplay', ()=>{
		preview_track.play()
		preview_stopped = false
		$(preview_track).animate({volume: preview_max_volume/100}, 500)
	})

	$(preview_track).on('timeupdate', ()=>{
		if (preview_track.currentTime > preview_track.duration-1){
			$(preview_track).animate({volume: 0}, 800)
			preview_stopped = true
			$('a[playing] > .preview_controls').css({opacity:0})
			$("*").removeAttr("playing")
			$('.preview_controls').text("play_arrow")
			$('.preview_playlist_controls').text("play_arrow")
		}
	})

	$('#modal_artist, #modal_trackListSelective').modal({
		onCloseStart: ()=>{
			if ($('.preview_playlist_controls').filter(function(){return $(this).attr("playing")}).length > 0){
				$(preview_track).animate({volume: 0}, 800)
				preview_stopped = true
				$(".preview_playlist_controls").removeAttr("playing")
				$('.preview_playlist_controls').text("play_arrow")
			}
		}
	})

	// Night Theme Switch
	$('#nightTimeSwitcher').change(function(){
		if(this.checked){
			document.getElementsByTagName('link')[4].disabled = false
			$("#nightModeSwitch2").html(`<i class="material-icons">brightness_7</i>${"Disable Night Mode"}`)
			localStorage.selectedTheme = "dark"
		}else{
			document.getElementsByTagName('link')[4].disabled = true
			$("#nightModeSwitch2").html(`<i class="material-icons">brightness_2</i>${"Enable Night Mode"}`)
			localStorage.selectedTheme = "light"
		}
	})

	$('#nightModeSwitch2').click((ev)=>{
		ev.preventDefault()
		$('#nightTimeSwitcher').prop('checked', !$('#nightTimeSwitcher').prop('checked'))
		$('#nightTimeSwitcher').change()
	})

	if (eval(localStorage.selectedTheme == "dark")){
		$('#nightTimeSwitcher').prop('checked', true)
		$('#nightTimeSwitcher').change()
	}else{
		$('#nightTimeSwitcher').prop('checked', false)
		$('#nightTimeSwitcher').change()
	}

	// Search on tab change
	$('input[name=searchMode][type=radio]').change(()=>{
		let url = $('#tab_search_form_search_input_searchString').val()
		if (url.indexOf('deezer.com/') < 0 && url.indexOf('open.spotify.com/') < 0 && url.indexOf('spotify:') < 0)
			$('#tab_search_form_search').submit()
	})
	$('#tab_search_form_search_input_searchString').on('input', function() {
		let url = $('#tab_search_form_search_input_searchString').val()
		if (url.indexOf('deezer.com/') < 0 && url.indexOf('open.spotify.com/') < 0 && url.indexOf('spotify:') < 0)
			$("#tab_search_button i").text("search")
		else
			$("#tab_search_button i").text("get_app")
	})

	// Enter on Link Analyzer and Link Download
	$('#link_analyzer_url').on("keyup", function(e) {
		if (e.keyCode == 13) {
			parseLinkAnalyzer($("#link_analyzer_url").val())
		}
	});

	// Button download all tracks in selective modal
	$('#download_all_tracks_selective, #download_all_tracks').on('contextmenu', function(e){
    e.preventDefault();
		$(modalQuality).data("url", $(this).attr("data-link"))
		$(modalQuality).css('display', 'block')
		$(modalQuality).addClass('animated fadeIn')
    return false;
	}).on('click', function(e){
    e.preventDefault();
		sendAddToQueue($(this).attr("data-link"))
		$(this).parent().parent().modal("close")
	})

	// Quality Modal
	window.onclick = function(event) {
	  if (event.target == modalQuality && modalQuality.open) {
			$(modalQuality).addClass('animated fadeOut')
	  }
	}
	$(modalQuality).on('webkitAnimationEnd', function () {
		if (modalQuality.open){
			$(this).removeClass('animated fadeOut')
			$(this).css('display', 'none')
			modalQuality.open = false
		}else{
			$(this).removeClass('animated fadeIn')
			$(this).css('display', 'block')
			modalQuality.open = true
		}
	})

	// Link Analyzer
	$("#link_analyzer_go").click(function(){
		parseLinkAnalyzer($("#link_analyzer_url").val())
	})

	// Settings cleanup
	$('#modal_settings_cbox_createPlaylistFolder').change(function(){
		$('#modal_settings_input_playlistNameTemplate').parent().slideToggle()
	})
	$('#modal_settings_cbox_createArtistFolder').change(function(){
		$('#modal_settings_input_artistNameTemplate').parent().slideToggle()
	})
	$('#modal_settings_cbox_createAlbumFolder').change(function(){
		$('#modal_settings_input_albumNameTemplate').parent().slideToggle()
	})
	$('#modal_settings_cbox_saveArtwork').change(function(){
		$('#modal_settings_input_coverImageTemplate').parent().slideToggle()
	})
	$('#modal_settings_cbox_saveArtworkArtist').change(function(){
		$('#modal_settings_input_artistImageTemplate').parent().slideToggle()
	})
	$('#modal_settings_cbox_createM3U8File').change(function(){
		$('#modal_settings_input_playlistFilenameTemplate').parent().slideToggle()
	})

	// Close Banner
	$(".close-banner").click(function(e){
		e.preventDefault();
		$(this).parent().slideUp()
	})
})

// Load settings
socket.on('init_settings', function(settings, spotify, defaultSettings){
	defaultUserSettings = defaultSettings
	userSettings = settings
	spotifySettings = spotify
	let spotifyUser = localStorage.getItem("spotifyUser")
	if (spotifyUser) socket.emit('update_userSpotifyPlaylists', spotifyUser)
	console.log('Settings initialized')
})

socket.on('updateSettings', function(settings, spotifyCredentials){
	userSettings = settings
	spotifySettings = spotifyCredentials
})

/**
 *	Modal Area START
 */

// Prevent default behavior of closing button
$('.modal-close').click(function (e) {
	e.preventDefault()
})

// Settings Modal START
const $settingsAreaParent = $('#modal_settings')

// Open settings panel
$('#nav_btn_openSettingsModal, #sidenav_settings').click(function () {
	fillSettingsModal(userSettings, spotifySettings)
})

// Save settings button
$('#modal_settings_btn_saveSettings').click(function () {
	let settings = {}
	// Save
	settings.userDefined = {
		downloadLocation: $('#modal_settings_input_downloadLocation').val(),
		tracknameTemplate: $('#modal_settings_input_tracknameTemplate').val(),
		albumTracknameTemplate: $('#modal_settings_input_albumTracknameTemplate').val(),
		playlistTracknameTemplate: $('#modal_settings_input_playlistTracknameTemplate').val(),
		createPlaylistFolder: $('#modal_settings_cbox_createPlaylistFolder').is(':checked'),
		playlistNameTemplate: $('#modal_settings_input_playlistNameTemplate').val(),
		createArtistFolder: $('#modal_settings_cbox_createArtistFolder').is(':checked'),
		artistNameTemplate: $('#modal_settings_input_artistNameTemplate').val(),
		createAlbumFolder: $('#modal_settings_cbox_createAlbumFolder').is(':checked'),
		albumNameTemplate: $('#modal_settings_input_albumNameTemplate').val(),
		createCDFolder: $('#modal_settings_cbox_createCDFolder').is(':checked'),
		createStructurePlaylist: $('#modal_settings_cbox_createStructurePlaylist').is(':checked'),
		createSingleFolder: $('#modal_settings_cbox_createSingleFolder').is(':checked'),
		padTracks: $('#modal_settings_cbox_padTracks').is(':checked'),
		paddingSize: $('#modal_settings_number_paddingSize').val(),
		illegalCharacterReplacer: $('#modal_settings_input_illegalCharacterReplacer').val(),
		queueConcurrency: parseInt($('#modal_settings_number_queueConcurrency').val()),
		maxBitrate: $('#modal_settings_select_maxBitrate').val(),
		fallbackBitrate : $('#modal_settings_cbox_fallbackBitrate').is(':checked'),
		fallbackSearch : $('#modal_settings_cbox_fallbackSearch').is(':checked'),
		logErrors: $('#modal_settings_cbox_logErrors').is(':checked'),
		logSearched: $('#modal_settings_cbox_logSearched').is(':checked'),
		saveDownloadQueue: $('#modal_settings_cbox_saveDownloadQueue').is(':checked'),
		overwriteFile: $('#modal_settings_select_overwriteFile').val(),
		createM3U8File: $('#modal_settings_cbox_createM3U8File').is(':checked'),
		playlistFilenameTemplate: $('#modal_settings_input_playlistFilenameTemplate').val(),
		syncedlyrics: $('#modal_settings_cbox_syncedlyrics').is(':checked'),
		embeddedArtworkSize: parseInt($('#modal_settings_select_embeddedArtworkSize').val()),
		embeddedArtworkPNG: $('#modal_settings_cbox_embeddedArtworkPNG').is(':checked'),
		localArtworkSize: parseInt($('#modal_settings_select_localArtworkSize').val()),
		localArtworkFormat: $('#modal_settings_select_localArtworkFormat').val(),
		saveArtwork: $('#modal_settings_cbox_saveArtwork').is(':checked'),
		coverImageTemplate: $('#modal_settings_input_coverImageTemplate').val(),
		saveArtworkArtist: $('#modal_settings_cbox_saveArtworkArtist').is(':checked'),
		artistImageTemplate: $('#modal_settings_input_artistImageTemplate').val(),
		jpegImageQuality: $('modal_settings_number_jpegImageQuality').val(),
		dateFormat: $('#modal_settings_select_dateFormat').val(),
		albumVariousArtists : $('#modal_settings_cbox_albumVariousArtists').is(':checked'),
		removeAlbumVersion : $('#modal_settings_cbox_removeAlbumVersion').is(':checked'),
		removeDuplicateArtists : $('#modal_settings_cbox_removeDuplicateArtists').is(':checked'),
		featuredToTitle: $('#modal_settings_select_featuredToTitle').val(),
		titleCasing : $('#modal_settings_select_titleCasing').val(),
		artistCasing : $('#modal_settings_select_artistCasing').val(),
		executeCommand: $('#modal_settings_input_executeCommand').val(),
		tags: {
			title: $('#modal_tags_title').is(':checked'),
			artist: $('#modal_tags_artist').is(':checked'),
			album: $('#modal_tags_album').is(':checked'),
			cover: $('#modal_tags_cover').is(':checked'),
			trackNumber: $('#modal_tags_trackNumber').is(':checked'),
			trackTotal: $('#modal_tags_trackTotal').is(':checked'),
			discNumber: $('#modal_tags_discNumber').is(':checked'),
			discTotal: $('#modal_tags_discTotal').is(':checked'),
			albumArtist: $('#modal_tags_albumArtist').is(':checked'),
			genre: $('#modal_tags_genre').is(':checked'),
			year: $('#modal_tags_year').is(':checked'),
			date: $('#modal_tags_date').is(':checked'),
			explicit: $('#modal_tags_explicit').is(':checked'),
			isrc: $('#modal_tags_isrc').is(':checked'),
			length: $('#modal_tags_length').is(':checked'),
			barcode: $('#modal_tags_barcode').is(':checked'),
			bpm: $('#modal_tags_bpm').is(':checked'),
			replayGain: $('#modal_tags_replayGain').is(':checked'),
			label: $('#modal_tags_label').is(':checked'),
			lyrics: $('#modal_tags_lyrics').is(':checked'),
			syncedLyrics: $('#modal_tags_syncedLyrics').is(':checked'),
			copyright: $('#modal_tags_copyright').is(':checked'),
			composer: $('#modal_tags_composer').is(':checked'),
			involvedPeople: $('#modal_tags_involvedPeople').is(':checked'),
			savePlaylistAsCompilation: $('#modal_settings_cbox_savePlaylistAsCompilation').is(':checked'),
			useNullSeparator : $('#modal_settings_cbox_useNullSeparator').is(':checked'),
			saveID3v1 : $('#modal_settings_cbox_saveID3v1').is(':checked'),
			multiArtistSeparator: $('#modal_settings_select_multiArtistSeparator').val(),
			singleAlbumArtist : $('#modal_settings_cbox_singleAlbumArtist').is(':checked'),
		}
	}
	let spotifyUser = $('#modal_settings_input_spotifyUser').val()
	let spotifyFeatures = {
		clientId: $('#modal_settings_input_spotifyClientID').val(),
		clientSecret: $('#modal_settings_input_spotifyClientSecret').val()
	}
	localStorage.setItem('spotifyUser', spotifyUser)
	// Send updated settings to be saved into config file
	//socket.emit('saveSettings', settings, spotifyFeatures, spotifyUser)
})

// Reset defaults button
$('#modal_settings_btn_defaultSettings').click(function () {
	fillSettingsModal(defaultUserSettings, spotifySettings)
})

// Populate settings fields
function fillSettingsModal(settings, spotifySettings = {clientId: "", clientSecret: ""}) {
	$('#modal_settings_input_downloadLocation').val(settings.downloadLocation)
	$('#modal_settings_input_tracknameTemplate').val(settings.tracknameTemplate)
	$('#modal_settings_input_albumTracknameTemplate').val(settings.albumTracknameTemplate)
	$('#modal_settings_input_playlistTracknameTemplate').val(settings.playlistTracknameTemplate)

	$('#modal_settings_cbox_createPlaylistFolder').prop('checked', settings.createPlaylistFolder)
	$('#modal_settings_input_playlistNameTemplate').val(settings.playlistNameTemplate)
	if (settings.createPlaylistFolder)
		$('#modal_settings_input_playlistNameTemplate').parent().slideDown()
	else
		$('#modal_settings_input_playlistNameTemplate').parent().slideUp()

	$('#modal_settings_cbox_createArtistFolder').prop('checked', settings.createArtistFolder)
	$('#modal_settings_input_artistNameTemplate').val(settings.artistNameTemplate)
	if (settings.createArtistFolder)
		$('#modal_settings_input_artistNameTemplate').parent().slideDown()
	else
		$('#modal_settings_input_artistNameTemplate').parent().slideUp()

	$('#modal_settings_cbox_createAlbumFolder').prop('checked', settings.createAlbumFolder)
	$('#modal_settings_input_albumNameTemplate').val(settings.albumNameTemplate)
	if (settings.createAlbumFolder)
		$('#modal_settings_input_albumNameTemplate').parent().slideDown()
	else
		$('#modal_settings_input_albumNameTemplate').parent().slideUp()

	$('#modal_settings_cbox_createCDFolder').prop('checked', settings.createCDFolder)
	$('#modal_settings_cbox_createStructurePlaylist').prop('checked', settings.createStructurePlaylist)
	$('#modal_settings_cbox_createSingleFolder').prop('checked', settings.createSingleFolder)
	$('#modal_settings_cbox_saveFullArtists').prop('checked', settings.saveFullArtists)
	$('#modal_settings_cbox_padTracks').prop('checked', settings.padTracks)
	$('#modal_settings_number_paddingSize').val(settings.paddingSize)
	$('#modal_settings_input_illegalCharacterReplacer').val(settings.illegalCharacterReplacer)
	$('#modal_settings_number_queueConcurrency').val(settings.queueConcurrency)
	$('#modal_settings_select_maxBitrate').val(settings.maxBitrate).formSelect()
	$('#modal_settings_cbox_fallbackBitrate').prop('checked', settings.fallbackBitrate)
	$('#modal_settings_cbox_fallbackSearch').prop('checked', settings.fallbackSearch)
	$('#modal_settings_cbox_logErrors').prop('checked', settings.logErrors)
	$('#modal_settings_cbox_logSearched').prop('checked', settings.logSearched)
	$('#modal_settings_cbox_saveDownloadQueue').prop('checked', settings.saveDownloadQueue)
	$('#modal_settings_select_overwriteFile').val(settings.overwriteFile).formSelect()
	$('#modal_settings_cbox_createM3U8File').prop('checked', settings.createM3U8File)
	$('#modal_settings_input_playlistFilenameTemplate').val(settings.playlistFilenameTemplate)
	if (settings.createM3U8File)
		$('#modal_settings_input_playlistFilenameTemplate').parent().slideDown()
	else
		$('#modal_settings_input_playlistFilenameTemplate').parent().slideUp()

	$('#modal_settings_cbox_syncedlyrics').prop('checked', settings.syncedlyrics)

	$('#modal_settings_select_embeddedArtworkSize').val(settings.embeddedArtworkSize).formSelect()
	$('#modal_settings_cbox_embeddedArtworkPNG').prop('checked', settings.embeddedArtworkPNG)
	$('#modal_settings_select_localArtworkSize').val(settings.localArtworkSize).formSelect()
	$('#modal_settings_select_localArtworkFormat').val(settings.localArtworkFormat).formSelect()

	$('#modal_settings_cbox_saveArtwork').prop('checked', settings.saveArtwork)
	$('#modal_settings_input_coverImageTemplate').val(settings.coverImageTemplate)
	if (settings.saveArtwork)
		$('#modal_settings_input_coverImageTemplate').parent().slideDown()
	else
		$('#modal_settings_input_coverImageTemplate').parent().slideUp()

	$('#modal_settings_cbox_saveArtworkArtist').prop('checked', settings.saveArtworkArtist)
	$('#modal_settings_input_artistImageTemplate').val(settings.artistImageTemplate)
	if (settings.saveArtworkArtist)
		$('#modal_settings_input_artistImageTemplate').parent().slideDown()
	else
		$('#modal_settings_input_artistImageTemplate').parent().slideUp()

	$('#modal_settings_number_jpegImageQuality').val(settings.jpegImageQuality)
	$('#modal_settings_select_dateFormat').val(settings.dateFormat).formSelect()
	$('#modal_settings_cbox_albumVariousArtists').prop('checked', settings.albumVariousArtists)
	$('#modal_settings_cbox_removeAlbumVersion').prop('checked', settings.removeAlbumVersion)
	$('#modal_settings_cbox_removeDuplicateArtists').prop('checked', settings.removeDuplicateArtists)
	$('#modal_settings_select_featuredToTitle').val(settings.featuredToTitle).formSelect()
	$('#modal_settings_select_titleCasing').val(settings.titleCasing).formSelect()
	$('#modal_settings_select_artistCasing').val(settings.artistCasing).formSelect()
	$('#modal_settings_input_executeCommand').val(settings.executeCommand)

	$('#modal_settings_input_spotifyUser').val(localStorage.getItem('spotifyUser'))
	$('#modal_settings_input_spotifyClientID').val(spotifySettings.clientId)
	$('#modal_settings_input_spotifyClientSecret').val(spotifySettings.clientSecret)

	$('#modal_tags_title').prop('checked', settings.tags.title)
	$('#modal_tags_artist').prop('checked', settings.tags.artist)
	$('#modal_tags_album').prop('checked', settings.tags.album)
	$('#modal_tags_cover').prop('checked', settings.tags.cover)
	$('#modal_tags_trackNumber').prop('checked', settings.tags.trackNumber)
	$('#modal_tags_trackTotal').prop('checked', settings.tags.trackTotal)
	$('#modal_tags_discNumber').prop('checked', settings.tags.discNumber)
	$('#modal_tags_discTotal').prop('checked', settings.tags.discTotal)
	$('#modal_tags_albumArtist').prop('checked', settings.tags.albumArtist)
	$('#modal_tags_genre').prop('checked', settings.tags.genre)
	$('#modal_tags_year').prop('checked', settings.tags.year)
	$('#modal_tags_date').prop('checked', settings.tags.date)
	$('#modal_tags_explicit').prop('checked', settings.tags.explicit)
	$('#modal_tags_isrc').prop('checked', settings.tags.isrc)
	$('#modal_tags_length').prop('checked', settings.tags.length)
	$('#modal_tags_barcode').prop('checked', settings.tags.barcode)
	$('#modal_tags_bpm').prop('checked', settings.tags.bpm)
	$('#modal_tags_replayGain').prop('checked', settings.tags.replayGain)
	$('#modal_tags_label').prop('checked', settings.tags.label)
	$('#modal_tags_lyrics').prop('checked', settings.tags.lyrics)
	$('#modal_tags_syncedLyrics').prop('checked', settings.tags.syncedLyrics)
	$('#modal_tags_copyright').prop('checked', settings.tags.copyright)
	$('#modal_tags_composer').prop('checked', settings.tags.composer)
	$('#modal_tags_involvedPeople').prop('checked', settings.tags.involvedPeople)

	$('#modal_settings_cbox_savePlaylistAsCompilation').prop('checked', settings.tags.savePlaylistAsCompilation)
	$('#modal_settings_cbox_useNullSeparator').prop('checked', settings.tags.useNullSeparator)
	$('#modal_settings_cbox_saveID3v1').prop('checked', settings.tags.saveID3v1)
	$('#modal_settings_select_multiArtistSeparator').val(settings.tags.multiArtistSeparator).formSelect()
	$('#modal_settings_cbox_singleAlbumArtist').prop('checked', settings.tags.singleAlbumArtist)

	M.updateTextFields()
}


//#############################################MODAL_MSG##############################################\\
function message(title, message) {
	$('#modal_msg_title').text(title)
	$('#modal_msg_message').html(message)
	$('#modal_msg').modal('open')
}

//****************************************************************************************************\\
//************************************************TABS************************************************\\
//****************************************************************************************************\\

//#############################################TAB_SEARCH#############################################\\

// Submit Search Form
$('#tab_search_form_search').submit(function (ev) {
	ev.preventDefault()
	var searchString = $('#tab_search_form_search_input_searchString').val().trim()
	if (searchString.indexOf('deezer.com/') < 0 && searchString.indexOf('open.spotify.com/') < 0 && searchString.indexOf('spotify:') < 0) {
		var mode = $('#tab_search_form_search').find('input[name=searchMode]:checked').val()
		searchData[mode].next = 0
		searchData[mode].total = 0
		currentSearch = searchString

		if (searchString.length == 0) {return}

		// Clean Table and show loading indicator
		$('#tab_search_table_results').find('thead').find('tr').addClass('hide')
		$('#tab_search_table_results_tbody_results').addClass('hide')
		$('#tab_search_table_results_tbody_noResults').addClass('hide')
		$('#tab_search_table_results_tbody_loadingIndicator').removeClass('hide')
		socket.emit("search", {type: mode, term: searchString, start: searchData[mode].next, nb: 30})
	}else{
		currentSearch = ""
		parseDownloadFromURL($('#tab_search_form_search_input_searchString').val().trim())
	}
})

$("#tab_search_button").on('contextmenu', function(e){
	e.preventDefault()
	var urls = $("#tab_search_form_search_input_searchString").val()
	if (urls.indexOf('deezer.com/') < 0 && urls.indexOf('open.spotify.com/') < 0 && urls.indexOf('spotify:') < 0) {
		return false;
	}
	let urlsArray = urls.split(";")
	if(urlsArray.length != 0){
		$(modalQuality).data("url", urls)
		$(modalQuality).css('display', 'block')
		$(modalQuality).addClass('animated fadeIn')
	}
	return false;
})

function parseDownloadFromURL(urlsString){
	urls = urlsString.split(";")
	newUrls = ""
	for(var i = 0; i < urls.length; i++){
		var url = urls[i]
		//Validate URL
		if (url.indexOf('deezer.com/') < 0 && url.indexOf('open.spotify.com/') < 0 && url.indexOf('spotify:') < 0) {
			continue
		}
		if (urls.length-1 != i)
			newUrls += url+";"
		else
			newUrls += url
	}
	sendAddToQueue(newUrls)
}

// Parse data from search
socket.on('search', function (data) {
	// Remove loading indicator
	$('#tab_search_table_results_tbody_loadingIndicator').addClass('hide')
	if (data.next) {
		next = parseInt(data.next.match(/index=(\d*)/)[1])
	} else {
		next = data.total
	}
	data.update = searchData[data.type].next != 0

	searchData[data.type].next = next
	if (!searchData[data.type].total) searchData[data.type].total = data.total

	// If no data, display No Results Found
	if (data.data.length == 0) {
		if (!data.update) $('#tab_search_table_results_tbody_noResults').removeClass('hide')
		return
	}

	// Populate table and show results
	if (data.type == 'track') {
		showResults_table_track(data.data, data.update)
	} else if (data.type == 'album') {
		showResults_table_album(data.data, data.update)
	} else if (data.type == 'artist') {
		showResults_table_artist(data.data, data.update)
	} else if (data.type == 'playlist') {
		showResults_table_playlist(data.data, data.update)
	}
	$('#tab_search_table_results_tbody_results').removeClass('hide')
})

function showResults_table_track(tracks, update) {
	var tableBody = $('#tab_search_table_results_tbody_results')
	if (!update) $(tableBody).html('')
	$('#tab_search_table_results_thead_track').removeClass('hide')
	for (var i = 0; i < tracks.length; i++) {
		var currentResultTrack = tracks[i]
		$(tableBody).append(
			`<tr>
			<td><a href="#" class="rounded ${(currentResultTrack.preview ? `single-cover" preview="${currentResultTrack.preview}"><i class="material-icons preview_controls white-text">play_arrow</i>` : '">')}<img style="width:56px;" class="rounded" src="${(currentResultTrack.album.cover_small ? currentResultTrack.album.cover_small : "img/noCover.jpg" )}"/></a></td>
			<td class="hide-on-med-and-up">
				<p class="remove-margin">${(currentResultTrack.explicit_lyrics ? ' <i class="material-icons valignicon tiny materialize-red-text">explicit</i>' : '')} ${currentResultTrack.title}</p>
				<p class="remove-margin secondary-text">${currentResultTrack.artist.name}</p>
				<p class="remove-margin secondary-text">${currentResultTrack.album.title}</p>
			</td>
			<td class="hide-on-small-only breakline">${(currentResultTrack.explicit_lyrics ? ' <i class="material-icons valignicon tiny materialize-red-text">explicit</i>' : '')} ${currentResultTrack.title}</td>
			<td class="hide-on-small-only breakline"><span class="resultArtist resultLink" data-link="${currentResultTrack.artist.link}">${currentResultTrack.artist.name}</span></td>
			<td class="hide-on-small-only breakline"><span class="resultAlbum resultLink" data-link="https://www.deezer.com/album/${currentResultTrack.album.id}">${currentResultTrack.album.title}</span></td>
			<td>${convertDuration(currentResultTrack.duration)}</td>
			</tr>`)
		generateDownloadLink(currentResultTrack.link).appendTo(tableBody.children('tr:last')).wrap('<td>')
		addPreviewControlsHover(tableBody.children('tr:last').find('.preview_controls'))
		addPreviewControlsClick(tableBody.children('tr:last').find('.single-cover'))
		tableBody.children('tr:last').find('.resultArtist').click(function (ev){
			ev.preventDefault()
			showArtistModal($(this).data("link"))
		})
		tableBody.children('tr:last').find('.resultAlbum').click(function (ev){
			ev.preventDefault()
			showTrackListSelective($(this).data("link"))
		})
	}
}

function showResults_table_album(albums, update) {
	var tableBody = $('#tab_search_table_results_tbody_results')
	if (!update) $(tableBody).html('')
	$('#tab_search_table_results_thead_album').removeClass('hide')
	for (var i = 0; i < albums.length; i++) {
		var currentResultAlbum = albums[i]
		$(tableBody).append(
				`<tr>
				<td><img style="width:56px;" src="${(currentResultAlbum.cover_small ? currentResultAlbum.cover_small : "img/noCover.jpg")}" class="rounded" /></td>
				<td class="hide-on-med-and-up">
					<p class="remove-margin">${(currentResultAlbum.explicit_lyrics ? `<i class="material-icons valignicon tiny materialize-red-text tooltipped" data-tooltip="${"Explicit"}">explicit</i> ` : '')} ${currentResultAlbum.title}</p>
					<p class="remove-margin secondary-text">${currentResultAlbum.artist.name}</p>
					<p class="remove-margin secondary-text">${currentResultAlbum.nb_tracks == "1" ? `1 Track` : `${currentResultAlbum.nb_tracks} Tracks`} • ${currentResultAlbum.record_type[0].toUpperCase() + currentResultAlbum.record_type.substring(1)}</p>
				</td>
				<td class="hide-on-small-only breakline">${(currentResultAlbum.explicit_lyrics ? `<i class="material-icons valignicon tiny materialize-red-text tooltipped" data-tooltip="${"Explicit"}">explicit</i> ` : '')} ${currentResultAlbum.title}</td>
				<td class="hide-on-small-only breakline"><span class="resultArtist resultLink" data-link="${currentResultAlbum.artist.link}">${currentResultAlbum.artist.name}</span></td>
				<td class="hide-on-small-only">${currentResultAlbum.nb_tracks}</td>
				<td class="hide-on-small-only">${currentResultAlbum.record_type[0].toUpperCase() + currentResultAlbum.record_type.substring(1)}</td>
				</tr>`)
		generateShowTracklistSelectiveButton(currentResultAlbum.link).appendTo(tableBody.children('tr:last')).wrap('<td>')
		generateDownloadLink(currentResultAlbum.link).appendTo(tableBody.children('tr:last')).wrap('<td>')
		tableBody.children('tr:last').find('.resultArtist').click(function (ev){
			ev.preventDefault()
			showArtistModal($(this).data("link"))
		})
	}
	$('.tooltipped').tooltip({delay: 100})
}

function showResults_table_artist(artists, update) {
	var tableBody = $('#tab_search_table_results_tbody_results')
	if (!update) $(tableBody).html('')
	$('#tab_search_table_results_thead_artist').removeClass('hide')
	for (var i = 0; i < artists.length; i++) {
		var currentResultArtist = artists[i]
		$(tableBody).append(
				`<tr>
				<td><img style="width:56px;" src="${(currentResultArtist.picture_small ? currentResultArtist.picture_small : "img/noCover.jpg")}" class="rounded" /></td>
				<td class="breakline">${currentResultArtist.name}</td>
				<td>${currentResultArtist.nb_album}</td>
				</tr>`)
		generateShowArtistButton(currentResultArtist.link).appendTo(tableBody.children('tr:last')).wrap('<td>')
		generateDownloadLink(currentResultArtist.link).appendTo(tableBody.children('tr:last')).wrap('<td>')
	}
}

function showResults_table_playlist(playlists, update) {
	var tableBody = $('#tab_search_table_results_tbody_results')
	if (!update) $(tableBody).html('')
	$('#tab_search_table_results_thead_playlist').removeClass('hide')
	for (var i = 0; i < playlists.length; i++) {
		var currentResultPlaylist = playlists[i]
		$(tableBody).append(
				`<tr>
				<td><img style="width:56px;" src="${(currentResultPlaylist.picture_small ? currentResultPlaylist.picture_small : "img/noCover.jpg")}" class="rounded" /></td>
				<td class="breakline">${currentResultPlaylist.title}</td>
				<td>${currentResultPlaylist.nb_tracks}</td>
				</tr>`)
		generateShowTracklistSelectiveButton(currentResultPlaylist.link).appendTo(tableBody.children('tr:last')).wrap('<td>')
		generateDownloadLink(currentResultPlaylist.link).appendTo(tableBody.children('tr:last')).wrap('<td>')
	}
	$('.tooltipped').tooltip({delay: 100})
}

// TODO: Finish Vue.js Implementation
var trackListSelectiveModalApp = new Vue({
	el: '#modal_trackListSelective',
	data: {
		title: "",
		metadata : "",
		release_date: "",
		label: "",
		explicit: false,
		image: "",
		type: "",
		link: "",
		head: null,
		body: []
	}
})

var artistModalApp = new Vue({
	el: '#modal_artist',
	data: {
		currentTab: '',
		sortKey: 'release_date',
		sortOrder: 'desc',
		title: "",
		image: "",
		type: "",
		link: "",
		head: null,
		body: null
	},
	methods: {
		downloadClick : function(url, e){
	    if (e) e.preventDefault();
	    sendAddToQueue(url)
		},
		downloadRClick: function(url, e){
	    if (e) e.preventDefault();
			$(modalQuality).data("url", url)
			$(modalQuality).css('display', 'block')
			$(modalQuality).addClass('animated fadeIn')
	    return false;
		},
		moreInfo: function(url, e){
			if (e) e.preventDefault();
			showTrackListSelective(url, true)
		},
		sortBy: function(key) {
      if (key == this.sortKey) {
				this.sortOrder = (this.sortOrder == 'asc') ? 'desc' : 'asc';
      } else {
				this.sortKey = key;
				this.sortOrder = 'asc';
      }
		},
		changeTab: function(tab){
			this.currentTab = tab
		},
		checkNewRelease: function(date){
			var g1 = new Date();
    	var g2 = new Date(date);
			g2.setDate(g2.getDate()+3)
			g1.setHours(0,0,0,0)
			if (g1.getTime() <= g2.getTime()){
				return true;
			}else {
				return false;
			}
		}
	},
	updated: function(){
		this.$nextTick(function () {
			if (this.body != {}){
				M.Tabs.init(document.getElementById("artist-tabs"));
			}
	  })
	},
	computed: {
		showTable() {
			return _.orderBy(this.body[this.currentTab], this.sortKey, this.sortOrder)
		}
	}
})

// Generate Button for tracklist with selection
function generateShowTracklistSelectiveButton(link) {
	var btn_showTrackListSelective = $('<button class="waves-effect btn-flat"><i class="material-icons">list</i></button>')
	$(btn_showTrackListSelective).click(function (ev){
		ev.preventDefault()
		showTrackListSelective(link)
	})
	return btn_showTrackListSelective
}

function showTrackListSelective(link) {
	$('#modal_trackListSelective_table_trackListSelective_tbody_trackListSelective').addClass('hide')
	$('#modal_trackListSelective_table_trackListSelective_tbody_loadingIndicator').removeClass('hide')
	trackListSelectiveModalApp.title = "Loading..."
	trackListSelectiveModalApp.image = ""
	trackListSelectiveModalApp.metadata = ""
	trackListSelectiveModalApp.label = ""
	trackListSelectiveModalApp.release_date = ""
	trackListSelectiveModalApp.explicit = false
	trackListSelectiveModalApp.type = ""
	trackListSelectiveModalApp.head = []
	trackListSelectiveModalApp.body = []
	$('#modal_trackListSelective').modal('open')
	let type = getTypeFromLink(link)
	let id = getIDFromLink(link, type)
	socket.emit('getTracklist', {id: id, type: type})
}

$('#download_track_selection').on('contextmenu', function(e){
	e.preventDefault();
	var urls = []
	$("input:checkbox.trackCheckbox:checked").each(function(){
		urls.push($(this).val())
	})
	if(urls.length != 0){
		urls = urls.join(";")
		$(modalQuality).data("url", urls)
		$(modalQuality).css('display', 'block')
		$(modalQuality).addClass('animated fadeIn')
	}
	return false;
}).on('click', function(e){
	e.preventDefault()
	var urls = []
	$("input:checkbox.trackCheckbox:checked").each(function(){
		urls.push($(this).val())
	})
	if(urls.length != 0){
		sendAddToQueue(urls.join(";"))
		$('#modal_trackListSelective').modal('close')
	}
})

// Generate Button for tracklist without selection
function generateShowArtistButton(link) {
	var btn_showTrackList = $('<button class="waves-effect btn-flat"><i class="material-icons">list</i></button>')
	$(btn_showTrackList).click(function (ev) {
		ev.preventDefault()
		showArtistModal(link)
	})
	return btn_showTrackList
}

function showArtistModal(link) {
	$('#modal_artist_table_trackList_tbody_trackList').addClass('hide')
	$('#modal_artist_table_trackList_tbody_noResults').addClass('hide')
	$('#modal_artist_table_trackList_tbody_loadingIndicator').removeClass('hide')
	artistModalApp.title = "Loading..."
	artistModalApp.image = ""
	artistModalApp.type = ""
	artistModalApp.currentTab = ''
	artistModalApp.sortKey = 'release_date'
	artistModalApp.sortOrder = 'desc'
	artistModalApp.link = link
	artistModalApp.head = []
	artistModalApp.body = null
	$('#modal_artist').modal('open')
	let type = "artist"
	let id = getIDFromLink(link, type)
	socket.emit('getTracklist', {id: id, type: type})
}

function parseAlbum(data){
	let trackList = data.tracks
	tableBody = $('#modal_trackListSelective_table_trackListSelective_tbody_trackListSelective')
	$(tableBody).html('')
	trackListSelectiveModalApp.type = "Album"
	trackListSelectiveModalApp.link = `https://www.deezer.com/album/${data.id}`
	trackListSelectiveModalApp.title = data.title
	trackListSelectiveModalApp.explicit = data.explicit_lyrics
	trackListSelectiveModalApp.label = data.label
	trackListSelectiveModalApp.metadata = `${data.artist.name} • ${trackList.length} songs`
	trackListSelectiveModalApp.release_date = data.release_date.substring(0,10)
	trackListSelectiveModalApp.image = data.cover_xl
	trackListSelectiveModalApp.head = [
		{title: '<i class="material-icons">music_note</i>', width: "24px"},
		{title: '#'},
		{title: 'Song'},
		{title: 'Artist', hideonsmall:true},
		{title: '<i class="material-icons">timer</i>', width: "40px"},
		{title: '<div class="valign-wrapper"><label><input class="selectAll" type="checkbox" id="selectAll"><span></span></label></div>', width: "24px"}
	]
	$('.selectAll').prop('checked', false)
	if (trackList[trackList.length-1].disk_number != 1){
		baseDisc = 0
	} else {
		baseDisc =1
	}
	let totalDuration = 0
	for (var i = 0; i < trackList.length; i++) {
		totalDuration += trackList[i].duration
		discNum = trackList[i].disk_number
		if (discNum != baseDisc){
			$(tableBody).append(`<tr><td colspan="4" style="opacity: 0.54;"><i class="material-icons valignicon tiny">album</i>${discNum}</td></tr>`)
			baseDisc = discNum
		}
		$(tableBody).append(
			`<tr>
			<td><i class="material-icons ${(trackList[i].preview ? `preview_playlist_controls" preview="${trackList[i].preview}"` : 'grey-text"')}>play_arrow</i></td>
			<td>${trackList[i].track_position}</td>
			<td class="hide-on-med-and-up">
				<p class="remove-margin">${(trackList[i].explicit_lyrics ? `<i class="material-icons valignicon tiny materialize-red-text tooltipped" data-tooltip="${"Explicit"}">explicit</i> ` : '')}${trackList[i].title}</p>
				<p class="remove-margin secondary-text">${trackList[i].artist.name}</p>
			</td>
			<td class="hide-on-small-only breakline">${(trackList[i].explicit_lyrics ? `<i class="material-icons valignicon tiny materialize-red-text tooltipped" data-tooltip="${"Explicit"}">explicit</i> ` : '')}${trackList[i].title}</td>
			<td class="hide-on-small-only breakline">${trackList[i].artist.name}</td>
			<td>${convertDuration(trackList[i].duration)}</td>
			<td>
				<div class="valign-wrapper">
				<label>
				<input class="trackCheckbox valign" type="checkbox" id="trackChk${i}" value="${trackList[i].link}"><span></span>
				</label>
				</div>
			</td>
			</tr>`
		)
		addPreviewControlsClick(tableBody.children('tr:last').find('.preview_playlist_controls'))
	}
	var [hh,mm,ss] = convertDurationSeparated(totalDuration)
	trackListSelectiveModalApp.metadata += `, ${hh>0 ? `${hh} hr` : ""} ${mm} min`
	$('#modal_trackListSelective_table_trackListSelective_tbody_loadingIndicator').addClass('hide')
	$('#modal_trackListSelective_table_trackListSelective_tbody_trackListSelective').removeClass('hide')
}
socket.on("show_album", parseAlbum)

function parsePlaylist(data){
	let trackList = data.tracks
	tableBody = $('#modal_trackListSelective_table_trackListSelective_tbody_trackListSelective')
	$(tableBody).html('')
	trackListSelectiveModalApp.type = "Playlist"
	trackListSelectiveModalApp.link = `https://www.deezer.com/playlist/${data.id}`
	trackListSelectiveModalApp.title = data.title
	trackListSelectiveModalApp.image = data.picture_xl
	trackListSelectiveModalApp.release_date = data.creation_date.substring(0,10)
	trackListSelectiveModalApp.metadata = `by ${data.creator.name} • ${trackList.length} songs`
	trackListSelectiveModalApp.head = [
		{title: '<i class="material-icons">music_note</i>', width: "24px"},
		{title: '#'},
		{title: 'Song'},
		{title: 'Artist', hideonsmall:true},
		{title: 'Album', hideonsmall:true},
		{title: '<i class="material-icons">timer</i>', width: "40px"},
		{title: '<div class="valign-wrapper"><label><input class="selectAll" type="checkbox" id="selectAll"><span></span></label></div>', width: "24px"}
	]
	$('.selectAll').prop('checked', false)
	let totalDuration = 0
	for (var i = 0; i < trackList.length; i++) {
		totalDuration += trackList[i].duration
		$(tableBody).append(
			`<tr>
			<td><i class="material-icons ${(trackList[i].preview ? `preview_playlist_controls" preview="${trackList[i].preview}"` : 'grey-text"')}>play_arrow</i></td>
			<td>${(i + 1)}</td>
			<td class="hide-on-med-and-up">
				<p class="remove-margin">${(trackList[i].explicit_lyrics ? `<i class="material-icons valignicon tiny materialize-red-text tooltipped" data-tooltip="${"Explicit"}">explicit</i> ` : '')}${trackList[i].title}</p>
				<p class="remove-margin secondary-text">${trackList[i].artist.name}</p>
			</td>
			<td class="hide-on-small-only breakline">${(trackList[i].explicit_lyrics ? `<i class="material-icons valignicon tiny materialize-red-text tooltipped" data-tooltip="${"Explicit"}">explicit</i> ` : '')}${trackList[i].title}</td>
			<td class="hide-on-small-only breakline"><span class="resultArtist resultLink" data-link="${trackList[i].artist.link}">${trackList[i].artist.name}</span></td>
			<td class="hide-on-small-only breakline"><span class="resultAlbum resultLink" data-link="https://www.deezer.com/album/${trackList[i].album.id}">${trackList[i].album.title}</span></td>
			<td>${convertDuration(trackList[i].duration)}</td>
			<td>
				<div class="valign-wrapper">
				<label>
				<input class="trackCheckbox valign" type="checkbox" id="trackChk${i}" value="${trackList[i].link}"><span></span>
				</label>
				</div>
			</td>
			</tr>`
		)
		addPreviewControlsClick(tableBody.children('tr:last').find('.preview_playlist_controls'))
		tableBody.children('tr:last').find('.resultArtist').click(function (ev){
			ev.preventDefault()
			showArtistModal($(this).data("link"))
		})
		tableBody.children('tr:last').find('.resultAlbum').click(function (ev){
			ev.preventDefault()
			showTrackListSelective($(this).data("link"))
		})
	}
	var [hh,mm,ss] = convertDurationSeparated(totalDuration)
	trackListSelectiveModalApp.metadata += `, ${hh>0 ? `${hh} hr` : ""} ${mm} min`
	$('#modal_trackListSelective_table_trackListSelective_tbody_loadingIndicator').addClass('hide')
	$('#modal_trackListSelective_table_trackListSelective_tbody_trackListSelective').removeClass('hide')
}
socket.on("show_playlist", parsePlaylist)

function parseSpotifyPlaylist(data){
	console.log(data)
	let trackList = data.tracks
	tableBody = $('#modal_trackListSelective_table_trackListSelective_tbody_trackListSelective')
	$(tableBody).html('')
	trackListSelectiveModalApp.type = "Spotify Playlist"
	trackListSelectiveModalApp.link = 'spotify:playlist:'+data.id
	trackListSelectiveModalApp.title = data.name
	trackListSelectiveModalApp.image = data.images[0].url
	trackListSelectiveModalApp.metadata = `by ${data.owner.display_name} • ${trackList.length} songs`
	trackListSelectiveModalApp.head = [
		{title: '<i class="material-icons">music_note</i>', width: "24px"},
		{title: '#'},
		{title: 'Song'},
		{title: 'Artist', hideonsmall:true},
		{title: '<i class="material-icons">timer</i>', width: "40px"},
		{title: '<div class="valign-wrapper"><label><input class="selectAll" type="checkbox" id="selectAll"><span></span></label></div>', width: "24px"}
	]
	let totalDuration = 0
	for (var i = 0; i < trackList.length; i++) {
		totalDuration += Math.round(trackList[i].duration_ms/1000)
		$(tableBody).append(
			`<tr>
			<td><i class="material-icons ${(trackList[i].preview_url ? `preview_playlist_controls" preview="${trackList[i].preview_url}"` : 'grey-text"')}>play_arrow</i></td>
			<td>${(i + 1)}</td>
			<td class="hide-on-med-and-up">
				<p class="remove-margin">${(trackList[i].explicit ? `<i class="material-icons valignicon tiny materialize-red-text tooltipped" data-tooltip="${"Explicit"}">explicit</i> ` : '')}${trackList[i].name}</p>
				<p class="remove-margin secondary-text">${trackList[i].artists[0].name}</p>
			</td>
			<td class="hide-on-small-only breakline">${(trackList[i].explicit ? `<i class="material-icons valignicon tiny materialize-red-text tooltipped" data-tooltip="${"Explicit"}">explicit</i> ` : '')}${trackList[i].name}</td>
			<td class="hide-on-small-only breakline">${trackList[i].artists[0].name}</td>
			<td>${convertDuration(Math.round(trackList[i].duration_ms/1000))}</td>
			<td>
				<div class="valign-wrapper">
				<label>
				<input class="trackCheckbox valign" type="checkbox" id="trackChk${i}" value="${trackList[i].uri}"><span></span>
				</label>
				</div>
			</td>
			</tr>`
		)
		addPreviewControlsClick(tableBody.children('tr:last').find('.preview_playlist_controls'))
	}
	var [hh,mm,ss] = convertDurationSeparated(totalDuration)
	trackListSelectiveModalApp.metadata += `, ${hh>0 ? `${hh} hr` : ""} ${mm} min`
	$('#modal_trackListSelective_table_trackListSelective_tbody_loadingIndicator').addClass('hide')
	$('#modal_trackListSelective_table_trackListSelective_tbody_trackListSelective').removeClass('hide')
}
socket.on("show_spotifyplaylist", parseSpotifyPlaylist)

function parseArtist(data){
	let trackList = data.releases
	artistModalApp.title = data.name
	artistModalApp.image = data.picture_xl
	artistModalApp.type = "Artist"
	artistModalApp.link = `https://www.deezer.com/artist/${data.id}`
	artistModalApp.currentTab = Object.keys(trackList)[0]
	artistModalApp.sortKey = 'release_date'
	artistModalApp.sortOrder = 'desc'
	artistModalApp.head = [
		{title: '', smallonly:true},
		{title: 'Title', hideonsmall:true, sortKey: "title"},
		{title: 'Release Date', hideonsmall:true, sortKey: "release_date"},
		{title: '', width: "56px"}
	]
	if (_.isEmpty(trackList)){
		artistModalApp.body = null
		$('#modal_artist_table_trackList_tbody_noResults').removeClass('hide')
	}else{
		artistModalApp.body = trackList
	}
	$('#modal_artist_table_trackList_tbody_loadingIndicator').addClass('hide')
}
socket.on("show_artist", parseArtist)

//#############################################TAB_CHARTS#############################################\\
socket.on("init_charts", function(charts){
	let selectedChart = localStorage.getItem("chart")
	for (var i = 0; i < charts.length; i++) {
		$('#tab_charts_select_country').append('<option value="' + charts[i].title + '" data-icon="' + charts[i].picture_small + '" data-id="' + charts[i].id + '" class="left rounded">' + charts[i].title + '</option>')
	}
	$('#tab_charts_select_country').find('option[value="' + selectedChart + '"]').attr("selected", true)
	$('select').formSelect()
	var country = $('#tab_charts_select_country').find('option:selected').data('id')
	var chart_id = $('#tab_charts_select_country').find('option:selected').data('id')
	socket.emit("getChartTracks", country)
	$("#downloadChartPlaylist").data("id", chart_id)
})

$('#tab_charts_select_country').on('change', function () {
	var chart_id = $(this).find('option:selected').data('id')
	var country = $(this).find('option:selected').val()
	localStorage.setItem('chart', country)
	$('#tab_charts_table_charts_tbody_charts').addClass('hide')
	$('#tab_charts_table_charts_tbody_loadingIndicator').removeClass('hide')
	socket.emit("getChartTracks", country)
	$("#downloadChartPlaylist").data("id", chart_id)
})

socket.on("setChartTracks", function (data) {
	var chartsTableBody = $('#tab_charts_table_charts_tbody_charts'), currentChartTrack
	chartsTableBody.html('')
	for (var i = 0; i < data.length; i++) {
		currentChartTrack = data[i]
		$(chartsTableBody).append(
				`<tr>
				<td>${(i + 1)}</td>
				<td><a href="#" class="rounded ${(currentChartTrack.preview ? `single-cover" preview="${currentChartTrack.preview}"><i class="material-icons preview_controls white-text">play_arrow</i>` : '">')}<img style="width:56px;" src="${(currentChartTrack.album.cover_small ? currentChartTrack.album.cover_small : "img/noCover.jpg")}" class="rounded" /></a></td>
				<td class="hide-on-med-and-up">
					<p class="remove-margin">${(currentChartTrack.explicit_lyrics ? `<i class="material-icons valignicon tiny materialize-red-text tooltipped" data-tooltip="${"Explicit"}">explicit</i> ` : '')}${currentChartTrack.title}</p>
					<p class="remove-margin secondary-text">${currentChartTrack.artist.name}</p>
					<p class="remove-margin secondary-text">${currentChartTrack.album.title}</p>
				</td>
				<td class="hide-on-small-only breakline">${(currentChartTrack.explicit_lyrics ? `<i class="material-icons valignicon tiny materialize-red-text tooltipped" data-tooltip="${"Explicit"}">explicit</i> ` : '')}${currentChartTrack.title}</td>
				<td class="hide-on-small-only breakline"><span class="resultArtist resultLink" data-link="${currentChartTrack.artist.link}">${currentChartTrack.artist.name}</span></td>
				<td class="hide-on-small-only breakline"><span class="resultAlbum resultLink" data-link="https://www.deezer.com/album/${currentChartTrack.album.id}">${currentChartTrack.album.title}</span></td>
				<td>${convertDuration(currentChartTrack.duration)}</td>
				</tr>`)
		generateDownloadLink(currentChartTrack.link).appendTo(chartsTableBody.children('tr:last')).wrap('<td>')
		addPreviewControlsHover(chartsTableBody.children('tr:last').find('.preview_controls'))
		addPreviewControlsClick(chartsTableBody.children('tr:last').find('.single-cover'))
		chartsTableBody.children('tr:last').find('.resultArtist').click(function (ev){
			ev.preventDefault()
			showArtistModal($(this).data("link"))
		})
		chartsTableBody.children('tr:last').find('.resultAlbum').click(function (ev){
			ev.preventDefault()
			showTrackListSelective($(this).data("link"))
		})
	}
	$('#tab_charts_table_charts_tbody_loadingIndicator').addClass('hide')
	chartsTableBody.removeClass('hide')
})

//#############################################TAB_PLAYLISTS############################################\\

function loadUserPlaylists(data, table){
	var tableBody = $(table)
	$(tableBody).html('')
	for (var i = 0; i < data.length; i++) {
		var currentResultPlaylist = data[i]
		$(tableBody).append(
				`<tr>
				<td><img src="${currentResultPlaylist.picture_medium || "/img/noCover.jpg"}" class="rounded" width="56px" /></td>
				<td>${currentResultPlaylist.title}</td>
				<td>${currentResultPlaylist.nb_tracks}</td>
				</tr>`)
		generateShowTracklistSelectiveButton(currentResultPlaylist.link).appendTo(tableBody.children('tr:last')).wrap('<td>')

		generateDownloadLink(currentResultPlaylist.link).appendTo(tableBody.children('tr:last')).wrap('<td>')
	}
	$('.tooltipped').tooltip({delay: 100})
}
socket.on("init_favorites", function(favorites) {
	loadUserPlaylists(favorites.playlists, '#table_personal_playlists')
})
socket.on("updated_userFavorites", function (favorites) {
	loadUserPlaylists(favorites.playlists, '#table_personal_playlists')
})
socket.on("updated_userSpotifyPlaylists", function (data) {
	loadUserPlaylists(data, '#table_personal_spotify_playlists')
})

//###############################################TAB_LINK#############################################\\

var linkAnalyzerSong = new Vue({
	el: '#link_analyzer_song',
	data: {
		d:{}
	},
	methods:{
		showArtist: function(){
			showArtistModal(this.d.artist.link)
		},
		showAlbum: function(){
			showTrackListSelective(`https://www.deezer.com/album/${this.d.album.id}`)
		}
	}
})

var linkAnalyzerAlbum = new Vue({
	el: '#link_analyzer_album',
	data: {
		d:{}
	},
	methods:{
		showArtist: function(){
			showArtistModal(`https://www.deezer.com/artist/${this.d.artist.id}`)
		}
	}
})

var linkAnalyzerCountryModal = new Vue({
	el: '#modal_link_analyzer_country',
	data: {
		title: "",
		countries: []
	}
})

function parseLinkAnalyzer(link){
	$("#link_analyzer_start").hide()
	$("#link_analyzer_notSupported").hide()
	$("#link_analyzer_album").hide()
	$("#link_analyzer_song").hide()
	$("#link_analyzer_loading").show()
	socket.emit('analyzeLink', link)
}

socket.on("analyze_track", (data)=>{
	data.countries_string = ""
	let countries = []
	data.available_countries.forEach((cc)=>{
		let temp = []
		let chars = [...cc].map(c => c.charCodeAt() + 127397)
		temp.push(String.fromCodePoint(...chars))
		temp.push(COUNTRIES[cc])
		countries.push(temp)
	})
	data.duration_string = convertDuration(data.duration)
	linkAnalyzerCountryModal.title = `${data.title}${data.title_version ? ` ${data.title_version}`: ""}`
	linkAnalyzerCountryModal.countries = countries
	linkAnalyzerSong.d = data
	$("#link_analyzer_loading").hide()
	$("#link_analyzer_song").show()
})

socket.on("analyze_album", (data)=>{
	let genres = []
	data.genres.data.forEach((genre)=>{
		genres.push(genre.name)
	})
	data.genres_string = genres.join(", ")
	data.duration_string = convertDuration(data.duration)
	data.tracks_string = data.nb_tracks+" songs"
	linkAnalyzerAlbum.d = data
	$("#link_analyzer_loading").hide()
	$("#link_analyzer_album").show()
})

socket.on("analyze_notSupported", function(){
	$("#link_analyzer_loading").hide()
	$("#link_analyzer_notSupported").show()
})

//############################################TAB_DOWNLOADS###########################################\\
function sendAddToQueue(url, bitrate=null) {
	if (!url) return
	socket.emit('addToQueue', { url, bitrate }, () => {})
}

function initQueue(data) {
	const { queue: initQueue, queueComplete: initQueueComplete, currentItem, queueList: initQueueList, restored } = data

	if (initQueueComplete.length) {
		initQueueComplete.forEach(item => {
			initQueueList[item].silent = true
			addToQueue(initQueueList[item])
		})
	}

	if (currentItem) {
		initQueueList[currentItem].silent = true
		addToQueue(initQueueList[currentItem], true)
	}

	initQueue.forEach(item => {
		initQueueList[item].silent = true
		addToQueue(initQueueList[item])
	})

	if (restored){
		toast("Queue Restored", 'done')
		socket.emit('queueRestored')
	}
}

function addToQueue(queueItem, current = false) {
	if (Array.isArray(queueItem)){
		if (queueItem.length > 1){
			queueItem.forEach((item, i) => {
				item.silent = true
				addToQueue(item)
			});
			toast(`Added ${queueItem.length} items to queue`, 'playlist_add_check')
			return
		}else{
			queueItem = queueItem[0]
		}
	}
	var tableBody = $('#tab_downloads_table_downloads').find('tbody')
	queueList[queueItem.uuid] = queueItem

	if (queueItem.downloaded + queueItem.failed == queueItem.size) {
		if (queueComplete.indexOf(queueItem.uuid) == -1) {
			queueComplete.push(queueItem.uuid)
		}
	} else {
		if (queue.indexOf(queueItem.uuid) == -1) {
			queue.push(queueItem.uuid)
		}
	}

	let queueDOM = document.getElementById('download_' + queueItem.uuid)

	if (typeof queueDOM == 'undefined' || queueDOM == null) {
		$(tableBody).append(
				`<tr class="download_object" id="download_${queueItem.uuid}" data-deezerid="${queueItem.id}" >
					<td class="download_cover">
						<img width="75px" src="${queueItem.cover}" alt="Cover ${queueItem.title}"/>
					</td>
					<td class="download_info_data">
						${queueItem.title}<br>
						<span class="secondary-text">${queueItem.artist}</span>
					</td>
					<td class="download_info_status">
						<span class="queue_downloaded">${queueItem.downloaded + queueItem.failed}</span>/${queueItem.size}<br>
					</td>
				</tr>
				<tr class="download_bar" id="download_bar_${queueItem.uuid}">
					<td colspan="4" class="progress"><div id="bar_${queueItem.uuid}" class="indeterminate"></div></td>
				</tr>`)
	}

	var btn_remove = $(`<button data-uuid="${queueItem.uuid}" class="btn-flat waves-effect"><i class="material-icons queue_icon">remove</i></button>`)

	$(btn_remove).click(function (ev) {
		ev.preventDefault()
		let resultIcon = $(this).find('.queue_icon')[0].innerText
		switch (resultIcon) {
			case 'remove':
				socket.emit("removeFromQueue", queueItem.uuid)
			break;
			case 'error':
			case 'warning':
				message(`Errors for ${quoteattr(queueItem.title)}`, queueItem.errorLog)
			break;
		}
	})

	btn_remove.appendTo(tableBody.children('tr.download_object:last')).wrap('<td class="eventBtn center">')

	if (queueItem.progress > 0 || current) {
		startDownload(queueItem.uuid)
	}

	$('#bar_' + queueItem.uuid).css('width', queueItem.progress + '%')

	if (queueItem.failed >= 1 && $('#download_' + queueItem.uuid + ' .queue_failed').length == 0) {
		$('#download_' + queueItem.uuid + ' .download_info_status').append(
			`<span class="secondary-text queue_failed_button"><span class="queue_failed">${queueItem.failed}</span> Failed</span>`
		)
	}

	if (queueItem.downloaded + queueItem.failed == queueItem.size) {
		let resultIcon = $('#download_' + queueItem.uuid).find('.queue_icon')

		if (queueItem.failed == 0) {
			resultIcon.text('done')
		} else {
			let failedButton = $('#download_' + queueItem.uuid).find('.queue_failed_button')

			resultIcon.addClass('clickable')
			failedButton.addClass('clickable')

			queueItem.errorLog = `<table><tr><th>ID</th><th>Song</th><th>Error</th></tr><tr><td>`
			queueItem.errors.forEach((error) => {
				queueItem.errorLog  += '<tr>'
				queueItem.errorLog  += `<td>${error.data.id}</td>`
				queueItem.errorLog  += `<td>${error.data.artist} - ${error.data.title}</td>`
				if (error.errid)
					queueItem.errorLog  += `<td>${error.errid}</td>`
				else
					queueItem.errorLog  += `<td>${error.message}</td>`
				queueItem.errorLog  += '</tr>'
			});
			queueItem.errorLog  += '</table>'

			if (queueItem.failed >= queueItem.size) {
				resultIcon.text('error')
			} else {
				resultIcon.text('warning')
			}
		}
	}

	if (!queueItem.silent) {
		toast(`${queueItem.title} added to queue!`, 'playlist_add_check')
	}
}

function updateQueue(update) {
	// downloaded and failed default to false?
	const { uuid, downloaded, failed, progress, conversion, error, data, errid } = update

	if (uuid && queue.indexOf(uuid) > -1) {
		if (downloaded) {
			queueList[uuid].downloaded++
			$('#download_' + uuid + ' .queue_downloaded').text(
				queueList[uuid].downloaded + queueList[uuid].failed
			)
		}

		if (failed) {
			queueList[uuid].failed++
			$('#download_' + uuid + ' .queue_downloaded').text(
				queueList[uuid].downloaded + queueList[uuid].failed
			)
			if (queueList[uuid].failed == 1 && $('#download_' + uuid + ' .queue_failed').length == 0) {
				$('#download_' + uuid + ' .download_info_status').append(
					`<span class="secondary-text queue_failed_button"><span class="queue_failed">${queueList[uuid].failed}</span> Failed</span>`
				)
			} else {
				$('#download_' + uuid + ' .queue_failed').text(queueList[uuid].failed)
			}

			queueList[uuid].errors.push({ message: error, data: data, errid: errid })
		}

		if (progress) {
			queueList[uuid].progress = progress
			$('#bar_' + uuid).css('width', progress + '%')
		}

		if (conversion) {
			$('#bar_' + uuid).css('width', (100-conversion) + '%')
		}
	}
}

function removeFromQueue(uuid) {
	let index = queue.indexOf(uuid)

	if (index > -1) {
		queue.splice(index, 1)
		$('#download_' + uuid+',#download_bar_' + uuid).addClass('animated fadeOutRight').on('webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend', function () {
			$(this).remove()
			delete queueList[uuid]
		})
	}
}

function removeAllDownloads(currentItem) {
	queueComplete = []

	if (currentItem == '') {
		queue = []
		queueList = {}
		$(listEl).html('')
	} else {
		queue = [currentItem]
		let tempQueueItem = queueList[currentItem]
		queueList = {}
		queueList[currentItem] = tempQueueItem

		$('.download_object,.download_bar').addClass('animated fadeOutRight').on('webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend', function () {
			if ($(this).attr('id') != 'download_' + currentItem) $(this).remove()
		})
	}
}

function removedFinishedDownloads() {
	queueComplete.forEach(item => {
		$('#download_' + item+',#download_bar_' + item).addClass('animated fadeOutRight').on('webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend', function () {
			$(this).remove()
		})
	})

	queueComplete = []
}

function finishDownload(uuid) {
	if (queue.indexOf(uuid) > -1) {
		toast(`${queueList[uuid].title} finished downloading`, 'done')

		$('#bar_' + uuid).css('width', '100%')

		let resultIcon = $('#download_' + uuid).find('.queue_icon')

		if (queueList[uuid].failed == 0) {
			resultIcon.text('done')
		} else {
			let failedButton = $('#download_' + uuid).find('.queue_failed_button')

			resultIcon.addClass('clickable')
			failedButton.addClass('clickable')

			queueList[uuid].errorLog = `<table><tr><th>ID</th><th>Song</th><th>Error</th></tr><tr><td>`
			queueList[uuid].errors.forEach((error) => {
				queueList[uuid].errorLog  += '<tr>'
				queueList[uuid].errorLog  += `<td>${error.data.id}</td>`
				queueList[uuid].errorLog  += `<td>${error.data.artist} - ${error.data.title}</td>`
				if (error.errid)
					queueList[uuid].errorLog  += `<td>${error.errid}</td>`
				else
					queueList[uuid].errorLog  += `<td>${error.message}</td>`
				queueList[uuid].errorLog  += '</tr>'
			});
			queueList[uuid].errorLog  += '</table>'

			if (queueList[uuid].failed >= queueList[uuid].size) {
				resultIcon.text('error')
			} else {
				resultIcon.text('warning')
			}
		}

		let index = queue.indexOf(uuid)
		if (index > -1) {
			queue.splice(index, 1)
			queueComplete.push(uuid)
		}

		if (queue.length <= 0) {
			toast("All downloads completed!", 'done_all')
		}
	}
}

function startDownload(uuid) {
	$('#bar_' + uuid)
		.removeClass('converting')
		.removeClass('indeterminate')
		.addClass('determinate')
}

function startConversion(uuid) {
	$('#bar_' + uuid)
		.addClass('converting')
		.removeClass('indeterminate')
		.addClass('determinate')
		.css('width', '100%')
}

socket.on('init_downloadQueue', initQueue)
socket.on('addedToQueue', addToQueue)
socket.on('updateQueue', updateQueue)
socket.on('removedFromQueue', removeFromQueue)
socket.on('removedAllDownloads', removeAllDownloads)
socket.on('removedFinishedDownloads', removedFinishedDownloads)
socket.on('finishDownload', finishDownload)
socket.on('startDownload', startDownload)
socket.on('startConversion', startConversion)

socket.on('errorMessage', function(error) {
	toast(error, 'error')
})

socket.on('queueError', function(queueItem) {
	if (queueItem.errid) toast(queueItem.errid, 'error')
	else toast(queueItem.error, 'error')
})

socket.on('alreadyInQueue', function(data) {
	toast(`${data.title} is already in queue`, 'playlist_add_check')
})

socket.on('loginNeededToDownload', function(data) {
	toast("You need to login first before downloading!", 'report')
})

$('#cancelAllTable').click(function (ev) {
	socket.emit('cancelAllDownloads')
})
$('#clearTracksTable').click(function (ev) {
	socket.emit('removeFinishedDownloads')
})

//****************************************************************************************************\\
//******************************************HELPER-FUNCTIONS******************************************\\
//****************************************************************************************************\\
/**
 * Replaces special characters with HTML friendly counterparts
 * @param s string
 * @param preserveCR preserves the new line character
 * @returns {string}
 */
function quoteattr(s, preserveCR) {
  preserveCR = preserveCR ? '&#13;' : '\n'
  return ('' + s) /* Forces the conversion to string. */
  	.replace(/&/g, '&amp;') /* This MUST be the 1st replacement. */
    .replace(/'/g, '&apos;') /* The 4 other predefined entities, required. */
    .replace(/"/g, '&quot;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    /*
    You may add other replacements here for HTML only
    (but it's not necessary).
    Or for XML, only if the named entities are defined in its DTD.
    */
    .replace(/\r\n/g, preserveCR) /* Must be before the next replacement. */
    .replace(/[\r\n]/g, preserveCR)

}

function getIDFromLink(link, type) {
	if (link.indexOf('?') > -1) {
		link = link.substring(0, link.indexOf("?"))
	}
	// Spotify
	if ((link.startsWith("http") && link.indexOf('open.spotify.com/') >= 0)){
		switch (type){
			case "spotifyplaylist":
				return link.slice(link.indexOf("/playlist/")+10)
				break
			case "spotifytrack":
				return link.slice(link.indexOf("/track/")+7)
				break
			case "spotifyalbum":
				return link.slice(link.indexOf("/album/")+7)
				break
		}
	} else if (link.startsWith("spotify:")){
		switch (type){
			case "spotifyplaylist":
				return link.slice(link.indexOf("playlist:")+9)
				break
			case "spotifytrack":
				return link.slice(link.indexOf("track:")+6)
				break
			case "spotifyalbum":
				return link.slice(link.indexOf("album:")+6)
				break
		}


	// Deezer
	} else if(type == "artisttop") {
		return link.match(/\/artist\/(\d+)\/top_track/)[1];
	} else {
		return link.substring(link.lastIndexOf("/") + 1)
	}
}

function getTypeFromLink(link) {
	var type
	if (link.indexOf('spotify') > -1){
		type = "spotify"
		if (link.indexOf('playlist') > -1) type += "playlist"
		else if (link.indexOf('track') > -1) type += "track"
		else if (link.indexOf('album') > -1) type += "album"
	} else	if (link.indexOf('/track') > -1) {
		type = "track"
	} else if (link.indexOf('/playlist') > -1) {
		type = "playlist"
	} else if (link.indexOf('/album') > -1) {
		type = "album"
	} else if (link.match(/\/artist\/(\d+)\/top_track/)) {
		type = "artisttop";
	} else if (link.indexOf('/artist')) {
		type = "artist"
	}
	return type
}

function generateDownloadLink(url) {
	var btn_download = $('<button class="waves-effect btn-flat" oncontextmenu="return false;"><i class="material-icons">file_download</i></button>')
	$(btn_download).on('contextmenu', function(e){
    e.preventDefault();
		$(modalQuality).data("url", url)
		$(modalQuality).css('display', 'block')
		$(modalQuality).addClass('animated fadeIn')
    return false;
	}).on('click', function(e){
	    e.preventDefault();
	    sendAddToQueue(url)
	})
	return btn_download
}

function modalQualityButton(bitrate){
	var url=$(modalQuality).data("url")
	sendAddToQueue(url, bitrate)
	$('#modal_trackListSelective').modal('close')
	$(modalQuality).addClass('animated fadeOut')
}

function addPreviewControlsHover(el){
	el.hover( function () {
		$(this).css({opacity: 1})
	}, function () {
		if (($(this).parent().attr("playing") && preview_stopped) || !$(this).parent().attr("playing")){
			$(this).css({opacity: 0}, 200)
		}
	})
}

function addPreviewControlsClick(el){
	el.click(function (e) {
		e.preventDefault()
		var icon = (this.tagName == "I" ? $(this) : $(this).children('i'))
		if ($(this).attr("playing")){
			if (preview_track.paused){
				preview_track.play()
				preview_stopped = false
				icon.text("pause")
				$(preview_track).animate({volume: preview_max_volume/100}, 500)
			}else{
				preview_stopped = true
				icon.text("play_arrow")
				$(preview_track).animate({volume: 0}, 250, "swing", ()=>{ preview_track.pause() })
			}
		}else{
			$("*").removeAttr("playing")
			$(this).attr("playing",true)
			$('.preview_controls').text("play_arrow")
			$('.preview_playlist_controls').text("play_arrow")
			$('.preview_controls').css({opacity:0})
			icon.text("pause")
			icon.css({opacity: 1})
			preview_stopped = false
			$(preview_track).animate({volume: 0}, 250, "swing", ()=>{
				preview_track.pause()
				$('#preview-track_source').prop("src", $(this).attr("preview"))
				preview_track.load()
			})
		}
	})
}

function convertDuration(duration) {
	//convert from seconds only to mm:ss format
	var mm, ss
	mm = Math.floor(duration / 60)
	ss = duration - (mm * 60)
	//add leading zero if ss < 0
	if (ss < 10) {
		ss = "0" + ss
	}
	return mm + ":" + ss
}

function convertDurationSeparated(duration){
	var hh, mm, ss
	mm = Math.floor(duration / 60)
	hh = Math.floor(mm / 60)
	ss = duration - (mm * 60)
	mm -= hh*60
	return [hh, mm, ss]
}

function sleep(milliseconds) {
  var start = new Date().getTime()
  for (var i = 0; i < 1e7; i++) {
    if ((new Date().getTime() - start) > milliseconds){
      break
		}
  }
}
