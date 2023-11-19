<template functional>
	<details :class="[data.staticClass, { 'with-arrow': props.withArrow }]" :style="data.staticStyle">
		<summary class="cursor-pointer">
			<slot name="title">Setting</slot>
		</summary>

		<div class="my-5 space-y-5">
			<slot name="default"></slot>
		</div>
	</details>
</template>

<style>
details > summary::-webkit-details-marker {
	display: none;
}

details.with-arrow > summary::-webkit-details-marker {
	display: initial;
	vertical-align: var(--arrow-v-align, super);
}
</style>

<script>
export default {
	props: {
		withArrow: {
			type: Boolean,
			default: true
		}
	}
}
</script>
