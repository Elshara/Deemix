<template>
	<div id="about_tab" class="main_tabcontent">
		<h2 class="page_heading">{{ $t('sidebar.about') }}</h2>
		<ul>
			<li v-html="$t('about.usesLibrary')"></li>
			<li v-html="$t('about.thanks')"></li>
			<li v-html="$t('about.upToDate')"></li>
		</ul>

		<h2>{{ $t('about.titles.usefulLinks') }}</h2>
		<ul class="no-dots">
			<li>
				<a href="https://deemix.app" target="_blank">🌍 {{ $t('about.officialWebsite') }}</a>
			</li>
			<li>
				<a href="https://codeberg.org/RemixDev/deemix" target="_blank">🚀 {{ $t('about.officialRepo') }}</a>
			</li>
			<li>
				<a href="https://codeberg.org/RemixDev/deemix-webui" target="_blank">💻 {{ $t('about.officialWebuiRepo') }}</a>
			</li>
			<li>
				<a href="https://www.reddit.com/r/deemix" target="_blank">🤖 {{ $t('about.officialSubreddit') }}</a>
			</li>
			<li>
				<a href="https://t.me/RemixDevNews" target="_blank">📰 {{ $t('about.newsChannel') }}</a>
			</li>
		</ul>

		<h2>
			{{ $t('about.titles.bugReports') }}
			<span class="subheading">
				{{ $t('about.subtitles.bugReports') }}
			</span>
		</h2>
		<ul>
			<li v-html="$t('about.questions')"></li>
			<li>
				{{ $t('about.beforeReporting') }}
			</li>
			<li v-html="$t('about.beSure')"></li>
			<li>
				{{ $t('about.duplicateReports') }}
			</li>
			<li v-html="$t('about.dontOpenIssues')"></li>
		</ul>

		<h2>
			{{ $t('about.titles.contributing') }}
			<span class="subheading">
				{{ $t('about.subtitles.contributing') }}
			</span>
		</h2>
		<ul>
			<li v-html="$t('about.newUI')"></li>
			<li>
				{{ $t('about.acceptFeatures') }}
			</li>
			<li v-html="$t('about.contributeWebUI')"></li>
			<li>
				{{ $t('about.otherLanguages') }}
			</li>
			<li>
				{{ $t('about.understandingCode') }}
			</li>
		</ul>

		<h2>
			{{ $t('about.titles.donations') }}
			<span class="subheading">
				{{ $t('about.subtitles.donations') }}
			</span>
		</h2>
		<ul>
			<li v-html="$t('about.itsFree')"></li>
			<li>
				{{ $t('about.notObligated') }}
			</li>
		</ul>
		<ul>
			<li>
				<i v-html="paypal" />
				<strong>PayPal:</strong>
				<a href="https://paypal.me/RemixDev" target="_blank">PayPal.me/RemixDev</a>
			</li>
			<li>
				<i class="bitcoin" v-html="bitcoin" />
				<strong>Bitcoin:</strong> 1sdNymSJrMBWyHM4u2m9uco5nv6uV4Qs1
			</li>
			<li>
				<i class="ethereum" v-html="ethereum" />
				<strong>Ethereum:</strong> 0x1d2aa67e671485CD4062289772B662e0A6Ff976c
			</li>
		</ul>

		<h2>{{ $t('about.titles.license') }}</h2>
		<p>
			<a rel="license" href="https://www.gnu.org/licenses/gpl-3.0.en.html" target="_blank">
				<img
					alt="GNU General Public License"
					style="border-width: 0;"
					src="https://www.gnu.org/graphics/gplv3-127x51.png"
				/>
			</a>
		</p>
		<p v-html="$t('about.lincensedUnder')"></p>
	</div>
</template>
<style lang="scss" scoped>
li,
p,
a {
	letter-spacing: 0.4px;
	font-size: 20px;
	line-height: 1.2;
}

i {
	vertical-align: middle;
}

i /deep/ svg {
	fill: white;
	width: 20px;
}

.bitcoin /deep/ svg {
	fill: #ff9900;
}

.ethereum /deep/ svg {
	fill: var(--foreground);
}

:link {
	text-decoration: none;
}

#about_tab {
	margin-bottom: 40px;
}

h2 {
	text-transform: capitalize;

	&:not(.page_heading) {
		font-size: 2rem;
		border-bottom: 1px solid hsla(0, 0%, 20%, 0.25);
		padding: {
			top: 2rem;
			bottom: 1rem;
		}
	}

	.subheading {
		display: block;
		font-size: 0.5em;
		margin-top: 0.5em;
		font-weight: normal;
		opacity: 0.8;
		text-transform: none;
	}
}

p {
	margin: 0 !important;
}

ul {
	li {
		margin-bottom: 7px;
	}

	h2 + & {
		margin-top: 1rem;
	}

	ul + & {
		margin-top: 1.25rem;
	}

	&.no-dots {
		list-style-type: none;
	}

	&:not(.no-dots) {
		list-style-type: none;

		li {
			position: relative;

			&::before {
				content: '—';
				position: absolute;
				left: -30px;
				opacity: 0.25;
			}
		}
	}
}
</style>
<script>
import paypal from '@/assets/paypal.svg'
import bitcoin from '@/assets/bitcoin.svg'
import ethereum from '@/assets/ethereum.svg'

export default {
	data: () => ({
		paypal,
		ethereum,
		bitcoin
	})
}
</script>
