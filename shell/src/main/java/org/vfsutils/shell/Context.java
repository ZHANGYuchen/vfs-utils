package org.vfsutils.shell;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;

public class Context {

	private Map map = new HashMap(10);

	public Context(FileSystemManager mgr) throws FileSystemException {
		
		FileObject cwd = mgr.resolveFile(System.getProperty("user.dir"));
		// don't use setCwd yet because the prompt does not yet exist
		set("vfs.cwd", cwd);
		setPrompt(new Prompt(this));
	}


	public FileObject getCwd() {
		return (FileObject) this.get("vfs.cwd");
	}

	public void setCwd(FileObject dir) {
		this.set("vfs.cwd", dir);
	}

	public Prompt getPrompt() {
		return (Prompt) this.get("vfs.prompt");
	}

	public void setPrompt(Prompt prompt) {
		this.set("vfs.prompt", prompt);
	}

	public Object get(String name) {
		return map.get(name);
	}

	public void set(String name, Object value) {
		map.put(name, value);
	}
	
	public void unset(String name) {
		map.remove(name);
	}
	
	public Map getAll() {
		return Collections.unmodifiableMap(map);
	}

}