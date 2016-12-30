package com.bondiBlue

class BufferPos {
	int line
	int word
	BufferPos (int l, int w){
		line = l
		word = w
	}
}

class Buffer {

	static enum CharType {
		Letter,
		Digit,
		Tab,
		Space,
		EOL,
		NewLine,
		Else
		static CharType get (char c) {
			if (Character.isLetter (c))
				return CharType.Letter
			else if (Character.isDigit (c))
				return CharType.Digit
			else if ((int)c == '\r')
				return CharType.EOL
			else if ((int)c == '\n')
				return CharType.NewLine
			else if (Character.isSpaceChar(c))
				return CharType.Space
			else if (c == '\t')
				return CharType.Tab
			else return CharType.Else
		}
	}

	static class Line{
		String indent
		ArrayList <String> words
		String terminator

		Line (){
			indent = ''
			words = new ArrayList<String>()
			terminator = ''
		}

	}

	ArrayList<Line> lines

	Buffer (){
		lines = new ArrayList<Line> ()
	}

	String getDebugString(String word)	{
		String text = ''
		def lenWord = word.length()
		for (int i = 0; i < lenWord; ++i){
			char c = word.charAt (i)
			CharType type = CharType.get (c)
			if (type == CharType.Tab)
				text += '->'
			else if (type == CharType.Space)
				text += '< >'
			else if (type == CharType.EOL)
				text += '<EOL>'
			else if (type == CharType.NewLine)
				text += '<CR>'
			else text += c
		}
		return text
	}

	Buffer (String input){
		lines = new ArrayList<Line> ()
		if (!input || input == '')
			return
		def line = new Line()

		String word = ''

		char c = input[0]
		word += c
		CharType prevType = CharType.get (c)
		CharType type

		def inputLen = input.length()
		for (int i = 1; i < inputLen; ++i){
			c = input[i]
			type = CharType.get (c)

			if (type != prevType) {
				line.words.add (word)
				word = ''
				if (prevType == CharType.NewLine){
					lines.add (line)
					line = new Line()
				}
			}
			word += c
			prevType = type
		}

		line.words.add (word)
		lines.add (line)

		for (def l : lines) {
			def ws = l.words
			if (ws[0][0]=='\t'){
				l.indent = ws[0]
				ws.remove (0)
			}
			if (ws.size() >= 2){
				if (ws[ws.size()-2]=='\r'){
					l.terminator = ws[ws.size()-2]
					ws.remove (ws.size()-2)
				}
				l.terminator += ws[ws.size()-1]
				ws.remove (ws.size() - 1)
			}
		}
	}

	String getString (){
		String ret = ''
		for (def line : lines){
			ret += line.indent
			for (def word : line.words)
				ret += word
			ret += line.terminator
		}
		return ret
	}

	void debug ()
	{
		println 'lines ' + lines.size()
		println '----------------------------------'
		for (def line : lines)
		{
			if (line.indent.size())
				println getDebugString (line.indent)
			println 'words ' + line.words.size()
			for (def word : line.words)
				println getDebugString (word)
			if (line.terminator.size())
				println getDebugString (line.terminator)
			println '----------------------------------'
		}
	}

	BufferPos getPos (BufferPos from, String word){
		if (!from)
			from = new BufferPos (0, 0)
		else {
			def line = lines[from.line]
			if (from.word < line.words.size() - 1)
				from.word ++
			else if (from.line < lines.size () - 1){
				from.line ++
				from.word = 0
			} else return null
		}

		def numLines = lines.size()
		for (int l = from.line; l < numLines; ++l){
			def line = lines[l]
			def numWords = line.words.size()
			def startw = l == from.line ? from.word : 0
			for (int w = startw; w < numWords; ++w)
				if (line.words[w] == word){
					from.line = l
					from.word = w
					return from
				}
		}
		return null
	}

	void add (Buffer buffer){
		lines += buffer.lines
	}

	void replace (String word, String newWord){
		BufferPos p = null
		while (p = getPos (p, word)){
			lines[p.line].words[p.word] = newWord
		}
	}

	void replace (String word, Buffer buffer){
		BufferPos p = null
		while (p = getPos (p, word)){
			def line = lines[p.line]
			def indent = line.indent
			def terminator = line.terminator
			lines.remove (p.line)
			def numLines = buffer.lines.size()
			for (int i = 0; i < numLines; ++i){
				def l = buffer.lines[i]
				def newLine = new Line()
				newLine.indent = indent + l.indent
				newLine.words.addAll (l.words)
				newLine.terminator = terminator
				lines.add (p.line + i, newLine)
			}
			p.line += numLines - 1
			p.word = lines[p.line].words.size () - 1
		}
	}

	void convertOpenBracket (boolean sameLine){
		BufferPos p = null
		while (p = getPos (p, '{')){
			if (p.line && p.word == lines[p.line].words.size() - 1){
				def prevLine = lines[p.line - 1]
				def line = lines[p.line]
				if (sameLine){
					prevLine.terminator = ''
					line.indent = ''
				}
				else{
					prevLine.terminator = line.terminator
					line.indent = prevLine.indent
				}
			}
		}
	}
}
