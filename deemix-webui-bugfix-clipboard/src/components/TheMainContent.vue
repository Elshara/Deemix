<template>
	<main id="main_content">
		<!-- <router-link to="/tracklist/132">Go to Foo</router-link> -->
		<!-- <router-view></router-view> -->
		<TheMiddleSection />
		<TheDownloadTab />
	</main>
</template>

<script>
import TheMiddleSection from '@components/TheMiddleSection.vue'
import TheDownloadTab from '@components/TheDownloadTab.vue'

export default {
	components: {
		TheMiddleSection,
		TheDownloadTab
	}
}
</script>

<style>
</style>