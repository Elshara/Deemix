import Vue from 'vue'
import VueI18n from 'vue-i18n'

// Languages
import it from '@/lang/it'
import en from '@/lang/en'
import es from '@/lang/es'
import de from '@/lang/de'
import fr from '@/lang/fr'
import id from '@/lang/id'
import pt from '@/lang/pt-pt'
import pt_br from '@/lang/pt-br'
import ru from '@/lang/ru'
import tr from '@/lang/tr'
import vn from '@/lang/vn'
import hr from '@/lang/hr'

Vue.use(VueI18n)

const DEFAULT_LANG = 'en'

document.querySelector('html').setAttribute('lang', DEFAULT_LANG)

const locales = {
	it,
	en,
	es,
	de,
	fr,
	id,
	pt,
	pt_br,
	ru,
	tr,
	vn,
	hr
}

const i18n = new VueI18n({
	locale: DEFAULT_LANG,
	fallbackLocale: DEFAULT_LANG,
	messages: locales,
	pluralizationRules: {
		/**
		 * @param choice {number} a choice index given by the input to $tc: `$tc('path.to.rule', choiceIndex)`
		 * @param choicesLength {number} an overall amount of available choices
		 * @returns a final choice index to select plural word by
		 */
		ru: function(choice, choicesLength) {
			var n = Math.abs(choice) % 100
			var n1 = n % 10

			if (n > 10 && n < 20) {
				return 2
			}

			if (n1 > 1 && n1 < 5) {
				return 1
			}

			if (n1 == 1) {
				return 0
			}

			return 2
		}
	}
})

export default i18n
