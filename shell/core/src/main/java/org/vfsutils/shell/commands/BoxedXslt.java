package org.vfsutils.shell.commands;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.vfsutils.shell.Engine;
import org.vfsutils.shell.boxed.BoxedVfsResolver;
import org.vfsutils.xml.VfsResolver;

public class BoxedXslt extends Xslt {

	protected VfsResolver newVfsResolver(FileObject file, Engine engine)
			throws FileSystemException {
		
		return new BoxedVfsResolver(file);
	}
	
	

}
