package org.vfsutils.selector;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.UriParser;

public class FilenameSelector implements FileSelector {
	
	private String pattern = null;
    private boolean casesensitive = true;
    private boolean negated = false;
    
    private int maxDepth = -1;
	
    
    
    
    /**
     * The name of the file, or the pattern for the name, that
     * should be used for selection.
     *
     * @param pattern the file pattern that any filename must match
     *                against in order to be selected.
     */
    public void setName(String pattern) throws FileSystemException {
        pattern = pattern.replace('/', FileName.SEPARATOR_CHAR).replace('\\',
                FileName.SEPARATOR_CHAR);
        if (pattern.endsWith(FileName.SEPARATOR)) {
            pattern += "**";
        }
        
        pattern = normalizePath(pattern);
        
        this.pattern = pattern;
        
        //count the depth 
		this.maxDepth = calculateDepth(pattern);
        
    }
    
    
    private int calculateDepth(String pattern) throws FileSystemException {
    	if (pattern.indexOf("**/")>-1) {
        	//a directory wildcard means no restriction
        	return -1;
        }
        else {
        	return pattern.split("/").length -1;
        }
    }
    
    /**
     * Replace '../' by '', '/./' bye '', '//' by '/'
     * @param pattern
     * @return
     */
    private String normalizePath(String pattern) throws FileSystemException {
    	StringBuffer buffer = new StringBuffer(pattern);
    	UriParser.normalisePath(buffer);
    	return buffer.toString();
    }
    
    /**
     * Whether to ignore case when checking filenames.
     *
     * @param casesensitive whether to pay attention to case sensitivity
     */
    public void setCasesensitive(boolean casesensitive) {
        this.casesensitive = casesensitive;
    }

    /**
     * You can optionally reverse the selection of this selector,
     * thereby emulating an &lt;exclude&gt; tag, by setting the attribute
     * negate to true. This is identical to surrounding the selector
     * with &lt;not&gt;&lt;/not&gt;.
     *
     * @param negated whether to negate this selection
     */
    public void setNegate(boolean negated) {
        this.negated = negated;
    }
    
	public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
		return (SelectorUtils.matchPath(pattern, fileInfo.getBaseFolder().getName().getRelativeName(fileInfo.getFile().getName()),
                casesensitive) == !(negated));
		
	}
	public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
		if (this.maxDepth==-1) {
			return true;
		}
		else {
			return fileInfo.getDepth()<=this.maxDepth;
		}
	}
	
	

}
