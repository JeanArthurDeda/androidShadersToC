package com.bondiBlue.androidShadersToC


class TaskInput {
	def File inputDir
	def File outputDir
	def String fileNameConv = 'lowerCamelCase'
	def boolean openBracketSameLine = false
	def String structPrefix = 't'
	def String structSuffix = ''
	def String structNameConv = 'lowerCamelCase'
	def String varNameConv = 'lowerCamelCase'
	def String staticVarPrefix = ''
	def String staticVarSuffix = 's'
	def String staticVarNameConv = 'goOutAnd_PLAY'
	def String packetPrefix = ''
	def String packetSuffix = 'shaders'
	def boolean shaderAddFlavourToVarName = true
	def String funcNameConv = 'lowerCamelCase'
	def String funcNewPrefix = ''
	def String funcNewSuffix = 'new'
	def String funcDeletePrefix = ''
	def String functDeleteSuffix = 'delete'
	String wc	= 'c'
	String wh	= 'h'
	String wGLint = 'GLint'
	String wGLuint = 'GLuint'
	boolean writeTemplates = false
	String templateDir = 'templates'
}
