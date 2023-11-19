import resolve from '@rollup/plugin-node-resolve'
import { terser } from 'rollup-plugin-terser'
import commonjs from '@rollup/plugin-commonjs'
import replace from '@rollup/plugin-replace'
import alias from '@rollup/plugin-alias'
import analyze from 'rollup-plugin-analyzer'
import vue from 'rollup-plugin-vue'
import svg from 'rollup-plugin-svg'

// 'rollup -c' -> 'production' is false
// 'rollup -c -w' -> 'production' is true
const production = !process.env.ROLLUP_WATCH

export default {
	input: 'src/app.js',
	output: [
		{
			file: 'public/js/bundle.js',
			format: 'module',
			sourcemap: !production
		}
	],
	plugins: [
		alias({
			entries: [
				{
					find: 'vue',
					replacement: 'vue/dist/vue.esm'
				},
				{
					find: '@',
					replacement: __dirname + '/src'
				},
				{
					find: '@js',
					replacement: __dirname + '/src/js'
				},
				{
					find: '@components',
					replacement: __dirname + '/src/components'
				}
			]
		}),
		// Needed for Vue imports
		replace({
			'process.env.NODE_ENV': JSON.stringify(production ? 'production' : 'development')
		}),
		resolve(), // Tells Rollup how to find imported modules in node_modules
		commonjs(), // Converts imported modules to ES modules, if necessary
		svg(),
		vue(),
		production && terser(), // Minify, but only in production
		production && analyze({ showExports: true, limit: 15 }) // Show useful information about bundles, only in production
	]
}
