(function() {

CKEDITOR.plugins.add(
	'audio',
	{
		afterInit: function(editor) {
			var dataProcessor = editor.dataProcessor;

			var	dataFilter = dataProcessor && dataProcessor.dataFilter;
			var	htmlFilter = dataProcessor && dataProcessor.htmlFilter;

			if (dataFilter) {
				dataFilter.addRules(
					{
						elements: {
							'div': function(realElement) {
								var attributeClass = realElement.attributes['class'];

								var fakeElement;

								if (attributeClass && attributeClass.indexOf('liferayckeaudio') >= 0) {
									var realChild = realElement.children && realElement.children[0];

									if (realChild &&
										realChild.attributes['class'].indexOf('ckaudio-no-id') != -1 &&
										realChild.children && realChild.children.length) {

										realChild.children[0].value = '';
									}

									fakeElement = editor.createFakeParserElement(realElement, 'liferay_cke_audio', 'audio', false);									
								}

								return fakeElement;
							}
						}
					}
				);
			}
			if (htmlFilter) {
				htmlFilter.addRules(
					{
						elements: {
							'div': function(realElement) {
								var attributeClass = realElement.attributes['class'];

								if (attributeClass && attributeClass.indexOf('ckaudio-no-id') >= 0 &&
									realElement.children && realElement.children.length) {

									realElement.children[0].value = '';
								}

								return realElement;
							}
						}
					}
				);
			}
		},

		getPlaceholderCss: function() {
			var instance = this;

			return 'img.liferay_cke_audio {' +
				'background: #CCC url(' + CKEDITOR.getUrl(instance.path + 'icons/placeholder.png') + ') no-repeat 50% 50%;' +
				'border: 1px solid #A9A9A9;' +
				'display: block;' +
				'height: 30px;' +
				'width: 100%;' +
			'}';
		},

		init: function(editor) {
			var instance = this;

			CKEDITOR.dialog.add('audio', instance.path + 'dialogs/audio.js');

			editor.addCommand('Audio', new CKEDITOR.dialogCommand('audio'));

			editor.ui.addButton(
				'Audio',
				{
					command: 'Audio',
					icon: instance.path + 'icons/icon.png',
					label: Liferay.Language.get('audio')
				}
			);

			if (editor.addMenuItems) {
				editor.addMenuItems(
					{
						audio: {
							command: 'Audio',
							group: 'flash',
							label: Liferay.Language.get('edit-audio')
						}
					}
				);
			}

			editor.on(
				'doubleclick',
				function(event) {
					var element = event.data.element;

					if (instance.isAudioElement(element)) {
						event.data.dialog = 'audio';
					}
				}
			);

			if (editor.contextMenu) {
				editor.contextMenu.addListener(
					function(element, selection) {
						var value = {};

						if (instance.isAudioElement(element) && !element.isReadOnly()) {
							value.audio = CKEDITOR.TRISTATE_OFF;
						}

						return value;
					}
				);
			}

			editor.lang.fakeobjects.audio = Liferay.Language.get('audio');
		},

		isAudioElement: function(el) {
			var instance = this;

			return (el && el.is('img') && el.data('cke-real-element-type') === 'audio');
		},

		onLoad: function() {
			var instance = this;

			if (CKEDITOR.addCss) {
				CKEDITOR.addCss(instance.getPlaceholderCss());
			}
		}
	}
);

})();