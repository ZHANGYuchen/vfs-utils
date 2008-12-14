package org.vfsutils.shell.commands;

import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public class Error extends AbstractCommand {

	public Error() {
		super("error", new CommandInfo("Error on last error", "[-p]"));
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, FileSystemException {
		
		if (args.getFlags().contains("p")) {
			printError(engine);
		}
		else {
			lastError(engine);
		}

	}

	public void lastError(Engine engine) {
		engine.println(engine.getLastError().getMessage());
	}
	
	public void printError(Engine engine) {
		engine.getLastError().printStackTrace(engine.getConsole().getOut());
	}
}
