package org.vfsutils.shell.commands;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public class Assert extends AbstractCommand {

	public Assert() {
		super("assert", new CommandInfo("Assert existence", "(-nfd)<path> [--cwd=<path>] [--scheme=<scheme>]"));
	}
	
	//TODO: it is better to use --file and --folder for matching the type (more like the find command)
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {
		
		args.assertSize(1);

		String path = args.getArgument(0);
				
        // Locate the file
        final FileObject file = engine.pathToFile(path);

        boolean notExists = args.hasFlag("n");
        boolean assertFile = args.hasFlag("f");
        boolean assertDir = args.hasFlag("d");
        
        String cwd = args.getOption("cwd");
        String scheme = args.getOption("scheme");
        
        assertExists(file, notExists, engine);
        
        if (assertFile || assertDir) {
	        assertType(file, assertFile, engine);
        }
        
        if (cwd!=null && cwd.length()>0) {
        	assertSame(engine.pathToExistingFile(cwd), engine.getCwd(), engine);
        }

        if (scheme!=null && scheme.length()>0) {
        	assertScheme(file, scheme);
        }
        
	}

	protected void assertExists(FileObject file, boolean negate, Engine engine) throws CommandException, FileSystemException {
		if (negate && file.exists()) {
       		throw new CommandException("File exists " + engine.toString(file));
        }
        else if (!file.exists()) {
        	throw new CommandException("File does not exist " + engine.toString(file));
        }
	}
	
	protected void assertType(FileObject file, boolean isFile, Engine engine) throws CommandException, FileSystemException {
		FileType type = file.getType(); 
        if (isFile) {
        	if (!(type.equals(FileType.FILE) || type.equals(FileType.FILE_OR_FOLDER))){
        		throw new CommandException("Not a file");
        	}
        }
        else {
        	if (!(type.equals(FileType.FOLDER) || type.equals(FileType.FILE_OR_FOLDER))){
        		throw new CommandException("Not a directory");
        	}
        }
	}

	protected void assertSame(FileObject expected, FileObject actual, Engine engine) throws CommandException {
		if (!expected.equals(actual)) {
			throw new CommandException("Expected " + engine.toString(expected) + " but received " + engine.toString(actual));
		}
	}
	
	protected void assertScheme(FileObject file, String scheme) throws CommandException {
		String actualScheme = file.getName().getScheme(); 
		if (!actualScheme.equals(scheme)) {
			throw new CommandException("Expected scheme " + scheme + " but found scheme " + actualScheme);
		}
	}
}
