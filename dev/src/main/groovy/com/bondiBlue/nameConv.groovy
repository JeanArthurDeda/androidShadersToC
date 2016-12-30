package com.bondiBlue

enum NameConvStyle {
	style,
	Style,
	STYLE;

	private final int index
	private static int startIndex
	NameConvStyle (){
		index = startIndex ++
	}
	int getIndex () {
		return index
	}
};

enum Separator {
	off,
	on

	private final int index
	private static int startIndex
	Separator (){
		index = startIndex ++;
	}
	int getIndex () {
		return index
	}
}


class NameConvFormat {
	public String	name
	public String alias

	public NameConvStyle firstStyle
	public Separator firstSeparator

	public NameConvStyle middleStyle
	public Separator middleSeparator

	public Separator lastSeparator
	public NameConvStyle lastStyle

	NameConvFormat (){}
}


class NameConv
{
	public static ArrayList<NameConvFormat> formats

	static public NameConvFormat getFormat (String theNameConvNameOrAlias) {
		if (!formats)
			genFormats ()
		for (int i = 0; i < formats.size (); ++i)
			if (formats[i].name == theNameConvNameOrAlias || formats[i].alias == theNameConvNameOrAlias)
				return formats[i]
		println 'NameConvFormat ' + theNameConvNameOrAlias + ' not found, falling back to ' + formats[0].name
		return formats[0]
	}

	static public void genFormats () {
		String[] firstWords = 'go Go GO'.split (' ')
		String[] middle1Words = 'out Out OUT'.split (' ')
		String[] middle2Words = 'and And AND'.split (' ')
		String[] lastWords = 'play Play PLAY'.split (' ')
		String[] separators = ' _'.split (' ')

		formats = new ArrayList<NameConvFormat> ()

		for (NameConvStyle firstStyle : NameConvStyle.values ())
			for (Separator firstSeparator : Separator.values ())
				for (NameConvStyle middleStyle : NameConvStyle.values ()) {
					if (
						((firstStyle == NameConvStyle.style || firstStyle == NameConvStyle.Style) && middleStyle == NameConvStyle.style && firstSeparator == Separator.off) ||
						(firstStyle == NameConvStyle.STYLE && (middleStyle == NameConvStyle.Style || middleStyle == NameConvStyle.STYLE) && firstSeparator == Separator.off)
						)
						continue
					for (Separator middleSeparator : Separator.values ()) {
						if (
							(middleStyle == NameConvStyle.style && middleSeparator == Separator.off) ||
							(middleStyle == NameConvStyle.STYLE && middleSeparator == Separator.off)
							)
							continue
						for (Separator lastSeparator : Separator.values ())
							for (NameConvStyle lastStyle : NameConvStyle.values ()) {
								if (
									((middleStyle == NameConvStyle.style || middleStyle == NameConvStyle.Style) && lastStyle == NameConvStyle.style && lastSeparator == Separator.off) ||
									(middleStyle == NameConvStyle.STYLE && (lastStyle == NameConvStyle.Style || lastStyle == NameConvStyle.STYLE) && lastSeparator == Separator.off)
									)
									continue
								NameConvFormat format = new NameConvFormat()

								format.firstStyle = firstStyle
								format.firstSeparator = firstSeparator
								format.middleStyle = middleStyle
								format.middleSeparator = middleSeparator
								format.lastSeparator = lastSeparator
								format.lastStyle = lastStyle

								format.name = firstWords[firstStyle.getIndex()] + separators[firstSeparator.getIndex ()] +
									middle1Words[middleStyle.getIndex()] + separators[middleSeparator.getIndex ()] + middle2Words[middleStyle.getIndex()] +
									separators[lastSeparator.getIndex ()] + lastWords[lastStyle.getIndex()];
								formats.add (format)
							}
					}
				}
		getFormat ('goOutAndPlay').alias = 'lowerCamelCase'
		getFormat ('goOutAndPLAY').alias = 'lowerCamelCaseSTYLE'
		getFormat ('goOutAnd_play').alias = 'lowerCamelCase_style'
		getFormat ('goOutAnd_Play').alias = 'lowerCamelCase_Style'
		getFormat ('goOutAnd_PLAY').alias = 'lowerCamelCase_STYLE'

		getFormat ('GoOutAndPlay').alias = 'UpperCamelCase'
		getFormat ('GoOutAndPLAY').alias = 'UpperCamelCaseSTYLE'
		getFormat ('GoOutAnd_play').alias = 'UpperCamelCase_style'
		getFormat ('GoOutAnd_Play').alias = 'UpperCamelCase_Style'
		getFormat ('GoOutAnd_PLAY').alias = 'UpperCamelCase_STYLE'

		getFormat ('go_out_and_play').alias = 'lazy_c'

		getFormat ('GO_OUT_AND_PLAY').alias = 'MACRO_C'

		getFormat ('GOout_andPLAY').alias = 'EyeS'

		getFormat ('goOUT_ANDplay').alias = 'aBBa'
	}

	static public void displayFormats () {
		if (!formats)
			genFormats ()
		println ('Naming convension formats(' + formats.size () + '):')
		for (int i = 0; i < formats.size (); ++i){
			NameConvFormat format = formats[i]
			if (format.alias)
				println format.name + ' a.k.a. ' + format.alias
			else
				println format.name
		}
	}

	static public String format (String name, NameConvFormat nameConv){
		join (split (name), nameConv)
	}

	static public String format (String prefix, String name, String suffix, NameConvFormat nameConv){
		def words = split (name)
		if (prefix != '')
			words.add (0, prefix)
		if (suffix != '')
			words.add (suffix)
		join (words, nameConv)
	}


	static public ArrayList<String> split (String name){
		if (!name || '' == name)
			return null

		ArrayList<String> words = new ArrayList<String>()

		String[] names = name.split ('_')
		for (int i = 0; i < names.length; ++i){
			String n = names[i]

			if (n == '')
				continue
			def lenN = n.length()
			if (lenN == 1){
				words.add (n);
				continue
			}

			int start = 0;

			char c = n.charAt (0);
			boolean prevIsUpper = c.isUpperCase ()
			int charCount = 1
			boolean isCammelCase = true
			for (int j = 1; j < lenN; ++j) {
				c = n.charAt (j)
				boolean isNumber = c.isDigit()
				boolean isUpper = isNumber ? prevIsUpper : c.isUpperCase();
				charCount ++

				if (charCount == 2){
					isCammelCase = isUpper != prevIsUpper
				}

				if (isCammelCase) {
					if (isUpper) {
						words.add (n.substring(start, j).toLowerCase())
						start = j
						charCount = 1
					}
				} else {
					if (isUpper != prevIsUpper) {
						words.add (n.substring(start, j).toLowerCase())
						start = j
						charCount = 1
					}
				}

				prevIsUpper = isUpper
			}
			words.add (n.substring (start).toLowerCase())
		}
		return words
	}

	static public String join (ArrayList<String> words, NameConvFormat format){
		String name = ''
		for (int i = 0; i < words.size (); ++ i) {
			String word = words[i].toLowerCase()

			NameConvStyle style = format.firstStyle
			if (i){
				if (i == words.size () - 1)
					style = format.lastStyle
				else
					style = format.middleStyle
			}

			if (style == NameConvStyle.Style)
				word = word.substring(0, 1).toUpperCase() + word.substring(1)
			else if (style == NameConvStyle.STYLE)
				word = word.toUpperCase()

			if (i){
				Separator sep = format.middleSeparator
				if (i == words.size () - 1)
					sep = format.lastSeparator
				else if (i == 1)
					sep = format.firstSeparator
				if (sep == Separator.on)
					name += '_'
			}

			name += word
		}

		return name
	}
}
