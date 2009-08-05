package org.vfsutils.shell.commands;

import java.util.Iterator;
import java.util.ListIterator;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;
import org.vfsutils.shell.Arguments.Argument;
import org.vfsutils.shell.Arguments.Flag;
import org.vfsutils.shell.Arguments.Option;

public class Register extends AbstractCommand {

	protected class Script extends AbstractCommand {
		
		String path = null;
		String type = "vfs";
		
		public Script(String cmd, CommandInfo info) {
			super(cmd, info);
		}

		public void execute(Arguments args, Engine engine)
				throws IllegalArgumentException, CommandException,
				FileSystemException {
			
			Arguments largs = copyArgs(this, args);
			engine.handleCommand(largs);
			
		}
		
	}
	
	public Register() {
		super("register", new CommandInfo("Registers a script", "<path>|<class> [--type={class|vfs|bsh}] [--name=<name>] [--description=<descr>] [--usage=<usage>] [--unregister]"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {
		
		args.assertSize(1);
		
		String type = args.getOption("type");
		
		String target = args.getArgument(0);
		String name = args.getOption("name");
		String description = args.getOption("description");
		String usage = args.getOption("usage");
		
		if ((type !=null && type.equals("class"))
			|| (type == null && isClassName(target))) {
			registerClass(target, name, description, usage, engine);
		}
		else {		
			FileObject[] files = engine.pathToFiles(target);
			
			if (files.length==0) {
				engine.println("No files selected");
			}
			else if (files.length==1) {
				registerScript(files[0], name, description, usage, type, engine);
			}
			else {
				registerScripts(files, type, engine);
			}
		}
	}
	
	protected void registerScripts(FileObject[] files, String type, Engine engine) {
		for (int i=0; i<files.length; i++) {
			registerScript(files[i], type, engine);
		}
	}
	
	protected void registerScript(FileObject file, String type, Engine engine) {
		registerScript(file, null, null, null, type, engine);
	}
	
	protected void registerScript(FileObject file, String name, String description, String usage, String type, Engine engine) {
		
		FileName fileName = file.getName();
		
		String extension = fileName.getExtension();
		
		if (name==null) {
			String baseName = fileName.getBaseName();
			name = (extension.length()==0?baseName:baseName.substring(0, baseName.length() - extension.length() -1));
		}
		
		if (description==null) {
			description = "script " + fileName.toString();
		}
		if (usage==null) {
			usage = "";
		}
		
		Script script = new Script(name, new CommandInfo(description, usage));
		script.path = fileName.toString();
		
		if (type!=null && (type.equals("vfs") || type.equals("bsh"))) {
			script.type = type;
		}
		else if (extension.equals("bsh")) {
			script.type = "bsh";
		}
		else {
			//the default
			script.type = "vfs";
		}
				
		script.register(engine.getCommandRegistry());
		engine.println("Registered " + script.type + " script " + fileName.toString() + " as " + name);	
		
	}

	public void registerClass(String className, String name, String description, String usage, Engine engine) throws CommandException {
		try {
			Class commandClass = Class.forName(className);
			if (CommandProvider.class.isAssignableFrom(commandClass)) {
				CommandProvider command = (CommandProvider) commandClass.newInstance();
	
				if (command instanceof AbstractCommand) {
					
					AbstractCommand abstractCommand = (AbstractCommand) command;
					if (name!=null) {
						abstractCommand.setCommand(name);					
					}
					
					if (description!=null) {
						abstractCommand.setDescription(description);
					}
					
					if (usage!=null) {
						abstractCommand.setUsage(usage);
					}
				}
				
				command.register(engine.getCommandRegistry());
				engine.println("Registered class " + className + " as " + command.getCommand());
			
			}
			else {
				throw new CommandException("Class " + className + " is not a valid Command");
			}
		}
		catch (CommandException e) {
			throw e;
		} 
		catch (Exception e) {
			throw new CommandException("Error while registering class " + className, e);
		}
	}
	
	
	protected Arguments copyArgs(Script script, Arguments args) {
		Arguments result = new Arguments();
		
		if (script.type.equals("bsh")) {
			result.setCmd("bsh");
		}
		else {
			result.setCmd("load");
		}
		
		result.addArgument(script.path);
		
		ListIterator argsIterator = args.getArguments().listIterator();
		while (argsIterator.hasNext()) {
			Argument arg = (Argument) argsIterator.next();
			result.addArgument(arg);
		}
		
		Iterator flagIterator = args.getFlags().iterator();
		while (flagIterator.hasNext()) {
			Flag flag = (Flag) flagIterator.next();
			result.addFlag(flag);
		}
		
		Iterator optionIterator = args.getOptions().keySet().iterator();
		while (optionIterator.hasNext()) {
			String key = (String) optionIterator.next();
			if (key.equals("flags")) {
				Option option = (Option) args.getOptions().get(key);
				char[] flags = option.getValue().toCharArray();
				//each flag should be added individually
				for (int i=0; i<flags.length; i++) {
					result.addFlag(String.valueOf(flags[i]));
				}
			}
			else {
				Option option = (Option) args.getOptions().get(key);
				result.addOption(option);
			}
		}			
		
		return result;
	}
	
	protected boolean isClassName(String input) {
			
		//test for slashes indicating it is a filename		
		if (input.indexOf("/")>-1) return false;
		
		//test for wildcards
		if (input.indexOf("*")>-1) return false;
		
		//more expensive: try to find class
		try {
			Class.forName(input);
		} catch (ClassNotFoundException e) {
			return false;
		}
		
		//is classname
		return true;
		
	}
}
