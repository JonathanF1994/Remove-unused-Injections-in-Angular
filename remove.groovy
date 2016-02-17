import java.util.regex.Matcher
import groovy.io.FileType

new File("PATH").eachFileRecurse (FileType.FILES) { file ->
    content = ""
    content = file.text
    init(content, file)
    
}

void init(content, file){
    if(checkForInject()){
        println file.name
        getInjects(content, file)
    }else{
    	file.newWriter().withWriter {w -> w << content}
    }
}

boolean checkForInject(){
    matcher = content =~ /(.*)(\..inject.*\])/
    if(matcher){
        functionName = matcher[0][1].replace(/ /, '')
        return true
    }else{
        return false
    }
}

void getInjects(content, file){
    list = []
    matcher[0][0].eachMatch(/\'(.*?)\'/){
        list.push(it[1])
    }
    injectionBlock = content =~ /inject.*?(\[.*?\])/
    if(injectionBlock[0][1].replace(/ /, '').length() > 2){
	    content = content.replace(/${injectionBlock[0][1]}/, 'WWW')
	    getInjects(content, list, file)  
    }
}

void getInjects(content, injects, file){
    def arr = []
    injects.each { entry ->
    	entry = entry.replace(/$/, 'qqq')
        def isUsedMatcher = content.replace(/$/, 'qqq') =~ /${entry}\.|${entry}\(/
        if(isUsedMatcher){
            entry = entry.replace(/qqq/, '$')
            arr.push(entry)
            arr = arr.sort()
        }else{
            injects = injects - entry
        }
    }
    buildReplaceString(content, arr, file)
}

void buildReplaceString(content, arr, file){
    replaceStringInject = ""
    replaceStringFunction = ""
    def functionBlock = content =~ /function ${functionName}(\(.*?\))/
    if(functionBlock){
        content = content.replace(/${functionBlock[0][1]}/, 'EEE')
    }
    arr.each {entry ->
        replaceStringInject = "${replaceStringInject}\'${entry}\',"
        replaceStringFunction = "${replaceStringFunction}${entry},"
    }
    replaceStringInject="[${replaceStringInject}]"
    replaceStringFunction="(${replaceStringFunction})"
    replaceContent(content, file)
}

void replaceContent(content, file){
	content = content.replace(/WWW/, replaceStringInject).replace(/,]/, ']')
    content = content.replace(/EEE/, replaceStringFunction).replace(/,)/, ')')
    file.newWriter().withWriter {w -> w << content}
}