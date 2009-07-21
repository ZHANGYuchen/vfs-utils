package org.vfsutils.shell.sshd;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.CommandFactory.Command;
import org.apache.sshd.server.command.ScpCommand;
import org.apache.sshd.server.command.UnknownCommand;

public class VfsScpCommandFactory implements CommandFactory {

    private CommandFactory delegate;
    private FileSystemManager fsManager;
    private String basePath;

    public VfsScpCommandFactory(FileSystemManager fsManager, String basePath) {
    	this.fsManager = fsManager;
    	this.basePath = basePath;
    }

    public VfsScpCommandFactory(CommandFactory delegate, FileSystemManager fsManager, String basePath) {
        this.delegate = delegate;
        this.fsManager = fsManager;
    	this.basePath = basePath;
    }

    public Command createCommand(String command){
        String[] args = command.split(" ");
        if (args.length > 0 && "scp".equals(args[0])) {
            return new VfsScpCommand(fsManager, basePath, args);
        }
        if (delegate != null) {
            return delegate.createCommand(command);
        }
        return new UnknownCommand(command);
    }

}

