const fr = {
	globals: {
		welcome: 'Bienvenue sur deemix',
		back: 'retour',
		loading: 'chargement',
		download: 'Téléchargement {0}',
		by: 'par {0}',
		in: 'en {0}',
		download_hint: 'Télécharger',
		play_hint: 'Lire',
		toggle_download_tab_hint: 'Développer/Réduire',
		clean_queue_hint: 'Retirer Les Tâches Terminées',
		cancel_queue_hint: 'Tout Annuler',
		listTabs: {
			empty: '',
			all: 'tout',
			top_result: 'meilleur résultat',
			album: 'album | albums',
			artist: 'artiste | artistes',
			single: 'single | singles',
			title: 'titre | titres',
			track: 'piste | pistes',
			trackN: '0 piste | {n} piste | {n} pistes',
			releaseN: '0 parution | {n} parution | {n} parutions',
			playlist: 'playlist | playlists',
			compile: 'compilation | compilations',
			ep: 'ep | eps',
			more: "Plus d'albums",
			featured: 'En vedette dans',
			spotifyPlaylist: 'playlist spotify | playlists spotify',
			releaseDate: 'date de parution',
			error: 'erreur'
		}
	},
	about: {
		titles: {
			usefulLinks: 'Liens Utiles',
			bugReports: 'Rapports De Bug',
			contributing: 'Contribution',
			donations: 'Dons',
			license: 'Licence'
		},
		subtitles: {
			bugReports: 'Y a-t-il quelque chose qui ne fonctionne pas dans deemix ? Dites-le nous !',
			contributing: 'Vous souhaitez contribuer à ce projet ? Vous pouvez le faire de différentes manières !',
			donations: 'Vous souhaitez contribuer financièrement ? Vous pouvez faire un don !'
		},
		usesLibrary:
			'Cette application utilise la bibliothèque <strong>deemix</strong>, que vous pouvez exploiter afin de créer votre propre interface utilisateur pour deemix.',
		thanks: "Merci à <strong>rtonno</strong>, <strong>uhwot</strong> et <strong>lollilol</strong> de m'avoir aidé dans ce projet ainsi qu'à <strong>BasCurtiz</strong> et <strong>scarvimane</strong> pour avoir réalisé l'icône.",
		upToDate: 'Restez informé des mises à jour en suivant le <a href="https://t.me/RemixDevNews" target="_blank">canal de nouveautés</a> sur Telegram.',
		officialWebsite: 'Site Officiel',
		officialRepo: 'Répertoire De Dépôt Officiel De La Bibiliothèque',
		officialWebuiRepo: 'Répertoire De Dépôt Officiel De La WebUI',
		officialSubreddit: 'Subreddit Officiel',
		newsChannel: 'Canal De Nouveautés',
		questions: `Si vous avez des questions ou des problèmes avec l'application, cherchez d'abord une solution dans le <a href="https://www.reddit.com/r/deemix" target="_blank">subreddit</a>. Ensuite, si vous ne trouvez rien, vous pouvez publier un message avec votre problème dans le subreddit.`,
		beforeReporting: "Avant de signaler un bug, assurez-vous que vous exécutez la dernière version de l'application et que ce que vous voulez signaler est bien un bug et non quelque chose qui ne va pas de votre côté.",
		beSure: "Assurez-vous que le bug est reproductible sur d'autres machines et aussi de <strong>NE PAS</strong> signaler un bug si celui-ci a déjà été mentionné.",
		duplicateReports: 'Les doublons de rapports de bug seront supprimés, alors gardez un œil sur cela.',
		dontOpenIssues: "<strong>NE PAS</strong> ouvrir un nouveau problème pour poser des questions, il existe un subreddit pour cela.",
		newUI: `Si vous maîtrisez python, vous pouvez essayer de créer une nouvelle interface utilisateur pour l'application à l'aide de la bibliothèque de base, ou corriger des bugs dans la bibliothèque à l'aide d'une demande de fusion de branches (pull request) sur le <a href="https://codeberg.org/RemixDev/deemix" target="_blank">répertoire de dépôt</a>.`,
		acceptFeatures: "J'accepte également les fonctionnalités, mais pas de choses complexes, car elles peuvent être implémentées directement dans l'application et non dans la bibliothèque.",
		otherLanguages: "Si vous maîtrisez un autre langage de programmation, vous pouvez essayer de transposer deemix dans d'autres langages de programmation !",
		understandingCode: "Vous avez besoin d'aide pour comprendre le code ? Il suffit de contacter RemixDev sur Telegram ou Reddit.",
		contributeWebUI: `Si vous vous y connaissez en Vue.js (JavaScript), HTML ou CSS vous pouvez contribuer à la <a href="https://codeberg.org/RemixDev/deemix-webui" target="_blank">WebUI</a>.`,
		itsFree: "N'oubliez pas que <strong>ceci est un projet gratuit</strong> et que <strong>vous devez soutenir les artistes que vous appréciez</strong> avant de supporter les développeurs.",
		notObligated: "Ne vous sentez pas obligé de faire un don, je vous apprécie quand même !",
		lincensedUnder: `Ce projet est autorisé dans le cadre d'une
			<a rel="license" href="https://www.gnu.org/licenses/gpl-3.0.fr.html" target="_blank">
				Licence publique générale GNU 3.0</a>.`
	},
	charts: {
		title: 'Hit-Parade',
		changeCountry: 'Changer De Pays',
		download: 'Télécharger Le Hit-Parade'
	},
	errors: {
		title: 'Erreurs pour {0}',
		ids: {
			invalidURL: "Cette URL n'est pas reconnue",
			unsupportedURL: "Cette URL n'est pas supportée actuellement",
			ISRCnotOnDeezer: "L'ISRC de la piste est indisponible sur Deezer",
			notYourPrivatePlaylist: "Vous ne pouvez pas télécharger les playlists privées de quelqu'un d'autre.",
			spotifyDisabled: 'Les Fonctionnalités Spotify ne sont pas configurées correctement.',
			trackNotOnDeezer: 'La piste est introuvable sur Deezer !',
			albumNotOnDeezer: "L'album est introuvable sur Deezer !",
			notOnDeezer: 'La piste est indisponible sur Deezer !',
			notEncoded: "La piste n'a pas encore été encodée !",
			notEncodedNoAlternative: "La piste n'a pas encore été encodée et aucune alternative n'a été trouvée !",
			wrongBitrate: 'La piste est introuvable au débit souhaité.',
			wrongBitrateNoAlternative: "La piste est introuvable au débit souhaité et aucune alternative n'a été trouvée !",
			no360RA: 'La piste est indisponible au format Reality Audio 360.',
			notAvailable: 'La piste est indisponible sur les serveurs de Deezer !',
			notAvailableNoAlternative: "La piste est indisponible sur les serveurs de Deezer et aucune alternative n'a été trouvée !"
		}
	},
	favorites: {
		title: 'Favoris',
		noPlaylists: "Aucune Playlist n'a été trouvée",
		noAlbums: "Aucun Album Favori n'a été trouvé",
		noArtists: "Aucun Artiste Favori n'a été trouvé",
		noTracks: "Aucune Piste Favorite n'a été trouvée"
	},
	home: {
		needTologin: 'Vous devez vous connecter à votre compte Deezer avant de pouvoir commencer les téléchargements.',
		openSettings: 'Ouvrir Les Paramètres',
		sections: {
			popularPlaylists: 'Playlists populaires',
			popularAlbums: 'Albums les plus diffusés'
		}
	},
	linkAnalyzer: {
		info:
			"Vous pouvez utiliser cette section pour obtenir plus d'informations sur le lien que vous essayez de télécharger.",
		useful:
			"C'est utile si vous essayer de télécharger des pistes qui ne sont pas disponibles dans votre pays et que vous souhaitez savoir où elles sont disponibles, par exemple.",
		linkNotSupported: "Ce lien n'est pas encore pris en charge",
		linkNotSupportedYet: "Il semble que ce lien ne soit pas encore pris en charge, essayez d'en analyser un autre.",
		table: {
			id: 'ID',
			isrc: 'ISRC',
			upc: 'UPC',
			duration: 'Durée',
			diskNumber: 'Numéro De Disque',
			trackNumber: 'Numéro De Disque',
			releaseDate: 'Date De Parution',
			bpm: 'BPM',
			label: 'Label',
			recordType: "Type d'Enregistrement",
			genres: 'Genres',
			tracklist: 'Liste Des Pistes'
		}
	},
	search: {
		startSearching: 'Commencer une recherche !',
		description:
			'Vous pouvez rechercher une piste, un album entier, un artiste, une playlist... tout ! Vous pouvez également copier-coller un lien Deezer',
		fans: '{0} fans',
		noResults: 'Aucun résultat',
		noResultsTrack: "Aucune piste n'a été trouvée",
		noResultsAlbum: "Aucun album n'a été trouvé",
		noResultsArtist: "Aucun artiste n'a été trouvé",
		noResultsPlaylist: "Aucune playlist n'a été trouvée"
	},
	searchbar: 'Recherchez tout ce que vous voulez (ou copiez-collez simplement un lien)',
	downloads: 'téléchargements',
	toasts: {
		addedToQueue: "{0} ajouté à la file d'attente",
		alreadyInQueue: "{0} est déjà en file d'attente !",
		finishDownload: '{0} a été téléchargé.',
		allDownloaded: 'Tous les téléchargements sont terminés !',
		refreshFavs: 'Actualisation terminée !',
		loggingIn: 'Connexion...',
		loggedIn: 'Connecté',
		alreadyLogged: 'Déjà connecté',
		loginFailed: 'Connexion impossible',
		loggedOut: 'Déconnecté',
		cancellingCurrentItem: "Annulation de l'élément actuel.",
		currentItemCancelled: 'Élément actuel annulé.',
		startAddingArtist: "Ajout de {0} albums en file d'attente",
		finishAddingArtist: "{0} albums ajoutés en file d'attente",
		startConvertingSpotifyPlaylist: 'Conversion de pistes Spotify en pistes Deezer',
		finishConvertingSpotifyPlaylist: 'Playlist Spotify convertie'
	},
	settings: {
		title: 'Paramètres',
		languages: 'Langues',
		login: {
			title: 'Connexion',
			loggedIn: 'Vous êtes connecté en tant que {username}',
			arl: {
				question: 'Comment puis-je obtenir mon ARL personnel ?',
				update: "Mettre à jour l'ARL"
			},
			logout: 'Déconnexion'
		},
		appearance: {
			title: 'Apparence',
			slimDownloadTab: 'Onglet de téléchargement plus petit'
		},
		downloadPath: {
			title: 'Emplacement De Téléchargement'
		},
		templates: {
			title: 'Gabarits',
			tracknameTemplate: 'Gabarit pour le nom de piste',
			albumTracknameTemplate: "Gabarit pour le nom de piste de l'album",
			playlistTracknameTemplate: 'Gabarit pour le nom de piste de la playlist'
		},
		folders: {
			title: 'Dossiers',
			createPlaylistFolder: 'Générer des dossiers par playlist',
			playlistNameTemplate: 'Gabarit pour le nom du dossier de playlist',
			createArtistFolder: 'Générer des dossiers par artiste',
			artistNameTemplate: "Gabarit pour le nom du dossier d'artiste",
			createAlbumFolder: 'Générer des dossiers par album',
			albumNameTemplate: "Gabarit pour le nom du dossier d'album",
			createCDFolder: 'Générer des dossiers par CD',
			createStructurePlaylist: 'Créer une structure de dossiers pour les playlists',
			createSingleFolder: 'Créer une structure de dossiers pour les singles'
		},
		trackTitles: {
			title: 'Titres de pistes',
			padTracks: "Longueur uniforme des numéros de piste (ajoute automatiquement des zéros devant le numéro initial de la piste)",
			paddingSize: 'Nombre de zéros à ajouter en permanance devant le numéro initial de la piste',
			illegalCharacterReplacer: 'Remplacement de caractère inapproprié'
		},
		downloads: {
			title: 'Téléchargements',
			queueConcurrency: 'Téléchargements Simultanés',
			maxBitrate: {
				title: 'Débit Préféré',
				9: 'FLAC 1411kbps',
				3: 'MP3 320kbps',
				1: 'MP3 128kbps'
			},
			overwriteFile: {
				title: 'Dois-je écraser les fichiers ?',
				y: 'Oui, écraser le fichier',
				n: 'Non, ne pas écraser le fichier',
				t: 'Écraser uniquement les métadonnées',
				b: 'Non, conserver les deux fichiers et ajouter un numéro au doublon'
			},
			fallbackBitrate: 'Recours aux débits plus faibles',
			fallbackSearch: 'Rechercher un débit plus faible',
			logErrors: "Créer un fichier journal d'erreurs",
			logSearched: 'Créer un fichier journal des pistes recherchées',
			createM3U8File: 'Créer un fichier de playlist',
			syncedLyrics: 'Créer des fichiers .lyr (Paroles Synchronisées)',
			playlistFilenameTemplate: 'Gabarit du nom de fichier de la playlist',
			saveDownloadQueue: "Enregistrer la file d'attente de téléchargement à la fermeture de l'application"
		},
		covers: {
			title: "Pochettes d'albums",
			saveArtwork: 'Enregistrer Les Pochettes',
			coverImageTemplate: 'Gabarit pour le nom de la pochette',
			saveArtworkArtist: "Enregistrer l'image de l'artiste",
			artistImageTemplate: "Gabarit pour le nom de l'image de l'artiste",
			localArtworkSize: "Taille de l'illustration locale",
			embeddedArtworkSize: "Taille de l'illustration incorporée",
			localArtworkFormat: {
				title: "Dans quel format souhaitez-vous l'illustration locale ?",
				jpg: 'Une image jpeg',
				png: 'Une image png',
				both: 'À la fois jpeg et png'
			},
			jpegImageQuality: "Qualité d'image JPEG"
		},
		tags: {
			head: 'Métadonnées à sauvegarder',
			title: 'Titre',
			artist: 'Artiste',
			album: 'Album',
			cover: 'Pochette',
			trackNumber: 'Numéro De Piste',
			trackTotal: 'Nombre De Pistes',
			discNumber: 'Numéro Du Disque',
			discTotal: 'Nombre De Disques',
			albumArtist: "Artiste De l'Album",
			genre: 'Genre',
			year: 'Année',
			date: 'Date',
			explicit: 'Paroles Explicites',
			isrc: 'ISRC',
			length: 'Longueur De La Piste',
			barcode: "Code-Barres De l'Album (UPC)",
			bpm: 'BPM',
			replayGain: 'Gain En Relecture (Replay Gain)',
			label: "Label De l'Album",
			lyrics: 'Paroles Non-Synchronisées',
			copyright: "Droits d'Auteur (Copyright)",
			composer: 'Compositeur',
			involvedPeople: 'Personnes Impliquées'
		},
		other: {
			title: 'Autre',
			savePlaylistAsCompilation: 'Enregistrer les playlists en tant que compilation',
			useNullSeparator: 'Utiliser un séparateur nul',
			saveID3v1: 'Enregistrer également les métadonnées ID3v1',
			multiArtistSeparator: {
				title: 'Comment aimeriez-vous séparer les artistes ?',
				nothing: "Enregistrer uniquement l'artiste principal",
				default: 'En utilisant la spécification standard',
				andFeat: 'En utilisant & et feat.',
				using: 'En utilisant "{0}"'
			},
			singleAlbumArtist: "Enregistrer uniquement l'artiste principal de l'album",
			albumVariousArtists: `Conserver "Various Artists" dans les Artistes de l'Album`,
			removeAlbumVersion: `Supprimer "Album Version" du titre de la piste`,
			removeDuplicateArtists: "Supprimer les combinaisons d'artistes",
			dateFormat: {
				title: 'Format de date pour les fichiers FLAC',
				year: 'AAAA',
				month: 'MM',
				day: 'JJ'
			},
			featuredToTitle: {
				title: 'Que dois-je faire avec les artistes participants (featuring) ?',
				0: 'Ne rien faire',
				1: 'Les retirer du titre de la piste',
				3: "Les supprimer du titre de la piste et du titre de l'album",
				2: 'Les déplacer vers le titre de la piste'
			},
			titleCasing: 'Casse pour le titre',
			artistCasing: "Casse pour l'artiste",
			casing: {
				nothing: 'Conserver inchangé',
				lower: 'minuscules',
				upper: 'MAJUSCULES',
				start: 'Majuscule Au Début De Chaque Mot',
				sentence: 'Majuscule seulement au début de la phrase'
			},
			previewVolume: 'Volume sonore des aperçus de pistes',
			executeCommand: {
				title: 'Commande à exécuter après le téléchargement',
				description: "Laisser vide pour qu'aucune action n'ait lieu"
			}
		},
		spotify: {
			title: 'Fonctionnalités Spotify',
			clientID: 'clientID Spotify',
			clientSecret: 'Client Secret Spotify',
			username: "Nom d'utilisateur Spotify"
		},
		reset: 'Rétablir les valeurs par défaut',
		save: 'Sauvegarder',
		toasts: {
			init: 'Paramètres chargés !',
			update: 'Paramètres mis à jour !',
			ARLcopied: 'ARL copié dans le presse-papier'
		}
	},
	sidebar: {
		home: 'accueil',
		search: 'recherche',
		charts: 'hit-parade',
		favorites: 'favoris',
		linkAnalyzer: 'analyseur de liens',
		settings: 'paramètres',
		about: 'à propos'
	},
	tracklist: {
		downloadSelection: 'Télécharger la sélection'
	}
}

export default fr
