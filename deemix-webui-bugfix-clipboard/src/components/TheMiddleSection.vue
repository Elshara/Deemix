<template>
	<div id="middle_section">
		<TheSearchBar />
		<TheContent />
		<BaseLoadingPlaceholder id="search_placeholder" text="Searching..." :hidden="true" />
	</div>
</template>

<script>
import TheContent from '@components/TheContent.vue'
import TheSearchBar from '@components/TheSearchBar.vue'
import BaseLoadingPlaceholder from '@components/BaseLoadingPlaceholder.vue'

export default {
	components: {
		TheContent,
		TheSearchBar,
		BaseLoadingPlaceholder
	}
}
</script>

<style>
</style>