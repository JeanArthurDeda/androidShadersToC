package com.bondiBlue.androidShadersToC

import static groovy.io.FileType.FILES
import com.bondiBlue.NameConv
import com.bondiBlue.NameConvFormat
import com.bondiBlue.Buffer
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.GradleException


class Task extends DefaultTask {
	@InputDirectory def File inputDir

	@OutputDirectory def File outputDir

	@Input def String filesNameConv = 'lowerCamelCase'

	@Input def boolean openBracketSameLine = false
	@Input def String structPrefix = 't'
	@Input def String structSuffix = ''
	@Input def String structNameConv = 'lowerCamelCase'

	@Input def String varsNameConv = 'lowerCamelCase'
	@Input def String staticVarsPrefix = ''
	@Input def String staticVarsSuffix = 's'
	@Input def String staticVarsNameConv = 'goOutAnd_PLAY'

	@Input def String packetNamePrefix = ''
	@Input def String packetNameSuffix = 'shaders'

	@Input def boolean shaderAddFlavourToVarName = true

	@Input def String functionsNameConv = 'lowerCamelCase'
	@Input def String functionNewPrefix = ''
	@Input def String functionNewSuffix = 'new'
	@Input def String functionDeletePrefix = ''
	@Input def String functionDeleteSuffix = 'delete'

	NameConvFormat		macroNCF
	NameConvFormat		filesNCF
	NameConvFormat		structNCF
	NameConvFormat		varsNCF
	NameConvFormat		staticVarsNCF
	NameConvFormat		functionsNCF

	@Input def String wc	= 'c'
	@Input def String wh	= 'h'
	@Input def String wGLint = 'GLint'
	@Input def String wGLuint = 'GLuint'

	String wprogram = 'program'
	String wshaders = 'shaders'
	String wpacket = 'packet'
	String wattribute = 'attribute'
	String wuniform = 'uniform'
	String wGL_VERTEX_SHADER = 'GL_VERTEX_SHADER'
	String wGL_FRAGMENT_SHADER = 'GL_FRAGMENT_SHADER'

	String formatedProgram = ''
	String formatedShaders = ''
	String formatedPacket = ''
	Map templates

	Map mapFolder (File folder){
		def map = [:]
		def files =folder.listFiles()
		for (def file : files){
			map[file.name] = file.isDirectory() ? mapFolder (file) : file
			// println 'map ' + file.path + ' to ' + file.path.replace ('\\', '.')
		}
		return map;
	}

	Task () {
		macroNCF		= NameConv.getFormat ('MACRO_C')
	}

	static private class Packet {
		String fileName
		boolean dirty
		String cFileName
		String hFileName
		String hMacro
		String structName
		ArrayList shaders
		ArrayList<File> shaderFiles
		ArrayList<String> shaderNames
		ArrayList<String> shaderVarNames
		ArrayList<String> shaderExts
		ArrayList<Boolean> shaderIsVS
		String functionNew
		String functionDelete

		public Packet (ArrayList packetShaders, String name){
			shaders = packetShaders
			fileName = name
			dirty = false
			shaderFiles = new ArrayList<File>()
			shaderNames = new ArrayList<String>()
			shaderVarNames = new ArrayList<String>()
			shaderExts = new ArrayList<String>()
			shaderIsVS = new ArrayList<Boolean>()
		}
	}

	void loadPacketShaders (Packet packet) {
		def names	= new ArrayList<String>()
		for (def shader : packet.shaders){
			boolean found = false
			for (def name : names) {
				if (name == shader.vertexShader) {
					found = true
					break
				}
			}
			if (!found) {
				names.add (shader.vertexShader)
				packet.shaderIsVS.add (true)
			}
			found = false
			for (def name : names) {
				if (name == shader.fragmentShader) {
					found = true
					break
				}
			}
			if (!found){
				names.add (shader.fragmentShader)
				packet.shaderIsVS.add (false)
			}
		}

		for (def name : names ) {
			def file = new File (inputDir, name)
			if (!file.exists () || file.isDirectory()){
				String error = 'Shader `' + name + '` refered from packet `' + packet.fileName + '` was not found or is a directory'
				println error
				throw new GradleException(error)
			}
			packet.shaderFiles.add (file)
			def fileName = name.substring (0, name.lastIndexOf('.')).toLowerCase ()
			def fileExt = name.substring (name.lastIndexOf('.') + 1).toLowerCase ()
			packet.shaderNames.add (fileName)
			packet.shaderExts.add  (fileExt)
			packet.shaderVarNames.add (NameConv.format (staticVarsPrefix, fileName+'_'+fileExt, staticVarsSuffix, staticVarsNCF))
		}

		for (int i = 0; i < names.size(); ++i) {
			def name = names[i]
			for (def shader : packet.shaders) {
				if (shader.vertexShader == name)
					shader.vsIndex = i
				if (shader.fragmentShader == name)
					shader.fsIndex = i
			}
		}
	}

	void genPacketNames (Packet packet) {
		packet.name = packet.fileName.substring(0, packet.fileName.lastIndexOf('.'))
		def composedPacketName = packetNamePrefix + '_' + packet.name + '_' + packetNameSuffix
		def fileName = NameConv.format (composedPacketName, filesNCF)
		packet.cFileName = fileName + '.' + wc
		packet.hFileName = fileName + '.' + wh
		packet.hMacro = NameConv.format ('', fileName, wh, macroNCF)

		packet.structName = NameConv.format (structPrefix, composedPacketName, structSuffix, structNCF)
		packet.functionNew = NameConv.format (functionNewPrefix, composedPacketName, functionNewSuffix, functionsNCF)
		packet.functionDelete = NameConv.format (functionDeletePrefix, composedPacketName, functionDeleteSuffix, functionsNCF)


		for (def shader : packet.shaders) {
			shader.structName	= NameConv.format (structPrefix, packet.name + '_' + shader.name, structSuffix, structNCF)
			shader.varName = NameConv.format (shader.name, varsNCF)
			shader.linkNames = new ArrayList<String> ()
			shader.linkFlavours = new ArrayList<String> ()
			shader.linkVarNames = new ArrayList<String> ()

			String[] vsTokens = packet.shaderFiles[shader.vsIndex].text.split ('[ \t\r\n]')
			boolean focus = false
			String flavour = ''
			String name = ''
			for (String t : vsTokens) {
				if ('' == t)
					continue
				if (t == wattribute || t == wuniform) {
					focus = true
					flavour = t
				} else if (focus && t.charAt (t.length()-1) == ';') {
					focus = false
					name  = t.substring (0, t.length () - 1)
					boolean found = false
					for (int i = 0; i < shader.linkNames.size (); ++i) {
						if (shader.linkNames[i] == name) {
							found = true
							break
						}
					}
					if (!found) {
						shader.linkNames.add (name)
						shader.linkFlavours.add (flavour)
						shader.linkVarNames.add (NameConv.format (shaderAddFlavourToVarName ? flavour : '', name, '', varsNCF))
					}
				}
			}

			String[] fsTokens = packet.shaderFiles[shader.fsIndex].text.split ('[ \t\r\n]')
			focus = false
			flavour = ''
			name = ''
			for (String t : vsTokens) {
				if (t == '')
					continue;
				if (t == wattribute || t == wuniform) {
					focus = true
					flavour = t
				} else if (focus && t.charAt (t.length()-1) == ';') {
					focus = false
					name  = t.substring (0, t.length () - 1)
					boolean found = false
					for (int i = 0; i < shader.linkNames.size (); ++i) {
						if (shader.linkNames[i] == name) {
							found = true
							break
						}
					}
					if (!found) {
						shader.linkNames.add (name)
						shader.linkFlavours.add (flavour)
						shader.linkVarNames.add (NameConv.format (shaderAddFlavourToVarName ? flavour : '', name, '', varsNCF))
					}
				}
			}
		}
	}

	void genPacketC (Packet packet) {
		println 'u\t' + packet.fileName + ' -> ' + packet.cFileName
		def file = new File (outputDir, packet.cFileName)

		def cfile = new Buffer (templates.cfile.text)

		def include = new Buffer (templates.blocks.include.text)
		include.replace ('name', packet.hFileName)
		cfile.replace ('include', include)

		def blocks = new Buffer()

		for (int i = 0; i < packet.shaderFiles.size (); ++i) {
			def staticVarDef = new Buffer (templates.blocks.staticVarDef.text)
			def name = packet.shaderVarNames[i]
			def value = packet.shaderFiles[i]
			staticVarDef.replace('type', 'char const * const')
			staticVarDef.replace('name', name)
			staticVarDef.replace('value', '"'+value.text+'"')

			def block = new Buffer (templates.blocks.hlineblock.text)
			block.replace ('block', staticVarDef)
			blocks.add (block)
		}

		def block = new Buffer (templates.blocks.hlineblock.text)
		def functionNew = new Buffer (templates.blocks.functionNew.text)
		functionNew.replace ('packetStruct', packet.structName)
		functionNew.replace ('functionNew', packet.functionNew)

		def listSrcs = new Buffer ()
		for (def name : packet.shaderVarNames){
			def enumItem = new Buffer (templates.blocks.enumItem.text)
			enumItem.replace ('item', name)
			listSrcs.add (enumItem)
		}
		functionNew.replace('listSrcs', listSrcs)

		def listTypes = new Buffer ()
		for (def isVS : packet.shaderIsVS){
			def enumItem = new Buffer (templates.blocks.enumItem.text)
			enumItem.replace ('item', isVS ? wGL_VERTEX_SHADER : wGL_FRAGMENT_SHADER)
			listTypes.add (enumItem)
		}
		functionNew.replace('listTypes', listTypes)

		def fillShaders = new Buffer ()
		int i = 0
		for (def shader : packet.shaders){
			def fillShader = new Buffer (templates.blocks.fillShader.text)
			fillShader.replace ('vsIndex', (String)shader.vsIndex)
			fillShader.replace ('fsIndex', (String)shader.fsIndex)
			def linkAttrs = new Buffer ()
			def linkUniforms = new Buffer ()
			int j = 0
			for (def linkName : shader.linkNames) {
				if (shader.linkFlavours[j] == wattribute){
					def linkAttr = new Buffer (templates.blocks.linkAttr.text)
					linkAttr.replace ('linkVarName', shader.linkVarNames[j])
					linkAttr.replace ('linkName', shader.linkNames[j])
					linkAttrs.add (linkAttr)
				}
				else {
					def linkUniform = new Buffer (templates.blocks.linkUniform.text)
					linkUniform.replace ('linkVarName', shader.linkVarNames[j])
					linkUniform.replace ('linkName', shader.linkNames[j])
					linkUniforms.add (linkUniform)
				}
				j ++
			}
			fillShader.replace ('linkAttrs', linkAttrs)
			fillShader.replace ('linkUniforms', linkUniforms)
			fillShader.replace ('shaderVarName', shader.varName)
			fillShaders.add (fillShader)
			i++
		}
		functionNew.replace ('fillShaders', fillShaders)

		block.replace ('block', functionNew)
		blocks.add (block)

		cfile.replace('blocks', blocks)

		cfile.replace (wpacket, formatedPacket)
		cfile.replace (wprogram, formatedProgram)
		cfile.convertOpenBracket(openBracketSameLine)
		file.text = cfile.getString()
	}

	void genPacketH (Packet packet){
		println 'u\t' + packet.fileName + ' -> ' + packet.hFileName
		def file = new File (outputDir, packet.hFileName)

		def header = new Buffer (templates.header.text)

		header.replace ('MACRO', packet.hMacro)

		def structs = new Buffer ()
		for (def shader : packet.shaders){
			def struct = new Buffer (templates.blocks.struct.text)
			struct.replace ('name', shader.structName)

			def varDecls = new Buffer ()
			def varDecl = new Buffer (templates.blocks.varDecl.text)
			varDecl.replace ('type', wGLuint)
			varDecl.replace ('name', wprogram)
			varDecls.add (varDecl)
			for (int i = 0; i < shader.linkNames.size(); ++i){
				def name = shader.linkVarNames[i]
				varDecl = new Buffer (templates.blocks.varDecl.text)
				varDecl.replace ('type', wGLint)
				varDecl.replace ('name', name)
				varDecls.add (varDecl)
			}
			struct.replace ('varDecls', varDecls)
			structs.add (struct)
		}
		def struct = new Buffer (templates.blocks.struct.text)
		struct.replace ('name', packet.structName)
		def varDecls = new Buffer ()
		for (def shader : packet.shaders){
			def varDecl = new Buffer (templates.blocks.varDecl.text)
			varDecl.replace ('type', shader.structName)
			varDecl.replace ('name', shader.varName)
			varDecls.add (varDecl)
		}
		def varDecl = new Buffer (templates.blocks.varDecl.text)
		varDecl.replace ('type', wGLuint)
		varDecl.replace ('name', formatedShaders+'['+packet.shaderFiles.size()+']')
		varDecls.add (varDecl)
		struct.replace ('varDecls', varDecls)
		structs.add (struct)
		header.replace ('structs', structs)

		header.replace ('packetStruct', packet.structName)
		header.replace ('functionNew', packet.functionNew)
		header.replace ('functionDelete', packet.functionDelete)

		header.replace (wpacket, formatedPacket)
		header.replace (wprogram, formatedProgram)
		header.convertOpenBracket(openBracketSameLine)
		file.text = header.getString()
	}

	void genPacket (Packet packet){
		println 'U ' + packet.fileName

		for (int i = 0; i < packet.shaders.size(); ++i)
			for (int j = 0; j < packet.shaders.size(); ++j)
				if (i != j && packet.shaders[i].name == packet.shaders[j].name){
					String error = 'Shader `' + packet.shaders[i].name + '` is defined more than once in packet `' + packet.fileName + '`'
					println error
					throw new GradleException(error)
				}

		loadPacketShaders (packet)
		genPacketNames (packet)
		genPacketC (packet)
		genPacketH (packet)
	}

	void deletePacket (String inputFileName){
		println 'X ' + inputFileName

		def packetName = inputFileName.substring(0, inputFileName.lastIndexOf('.'))
		def fileName = NameConv.format (packetName, filesNCF)
		def cFileName = fileName + '.' + wc
		def hFileName = fileName + '.' + wh
		println 'x\t' + inputFileName + ' -> ' + cFileName
		new File (outputDir, cFileName).delete ()
		println 'x\t' + inputFileName + ' -> ' + hFileName
		new File (outputDir, hFileName).delete ()
	}

	@TaskAction
	void execute (IncrementalTaskInputs inputs) {
		filesNCF = NameConv.getFormat (filesNameConv)
		structNCF = NameConv.getFormat (structNameConv)
		varsNCF = NameConv.getFormat (varsNameConv)
		staticVarsNCF = NameConv.getFormat (staticVarsNameConv)
		functionsNCF = NameConv.getFormat (functionsNameConv)
		formatedProgram = NameConv.format (wprogram, varsNCF)
		formatedShaders = NameConv.format (wshaders, varsNCF)
		formatedPacket = NameConv.format (wpacket, varsNCF)

		templates = mapFolder (new File ('templates'))

		def packets = new ArrayList<Packet> ()
		inputDir.eachFileRecurse (FILES) {
			if (it.name.endsWith('.json')) {
				println 'L ' + it.name
				def shaders = new groovy.json.JsonSlurper().parseText(it.text)
				Packet packet = new Packet (shaders, it.name)
				packets.add (packet)
			}
		}

		def changes = new ArrayList<File> ()
		inputs.outOfDate { change ->
			changes.add (change.file)
		}
		inputs.removed { change ->
			deletePacket (change.file.name)
		}

		for (def packet : packets){
			if (!inputs.incremental){
				packet.dirty = true
			} else
				for (def change : changes){
					if (packet.fileName == change.name) {
						println 'm ' + change.name
						packet.dirty = true
						break
					}
					for (def shader : packet.shaders){
						if (shader.vertexShader == change.name || shader.fragmentShader == change.name)
						{
							packet.dirty = true
							break
						}
					}
					if (packet.dirty){
						break
					}
				}
			if (packet.dirty)
				genPacket (packet)
		}

	}
}