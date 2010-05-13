package org.vfsutils.shell.commands;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;

public class Ls extends AbstractCommand implements CommandProvider {

	private String longListType = "nix";
	// Note that DateFormat.format is not threadsafe and so we can't store an
	// instance
	private String dateFormat = "dd/MM/yyyy HH:mm";

	public Ls() {
		super("ls", new CommandInfo("Directory listing", "[-l] [<path>]"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {

		FileObject file = null;
		
		boolean longList = args.hasFlag("l");
		
		if (args.size() > 0) {
			String path = args.getArgument(0);
			if (path.indexOf('*') > -1) {
				file = engine.getCwd();
				FileObject[] files = engine.pathToFiles(path);

				// List the contents
				listChildren(file, files, true, longList, engine);
			} else {
				file = engine.pathToFile(path);
				if (file.getType().hasChildren()) {
					// can throw a FileNotFolderException
					FileObject[] files = file.getChildren();
					// List the contents
					listChildren(file, files, false, longList, engine);
				} else {
					listChild(engine.getCwd(), file, true, false, longList, engine);
				}
			}
		} else {
			file = engine.getCwd();
			// can throw a FileNotFolderException
			FileObject[] files = file.getChildren();
			// List the contents
			listChildren(file, files, false, longList, engine);
		}

	}

	/**
	 * Lists the given files
	 */
	private void listChildren(final FileObject dir, final FileObject[] files,
			boolean showRelativePath, boolean longList, Engine engine)
			throws FileSystemException {

		engine.println("Contents of " + engine.toString(dir));
		
		int nrOfFiles = 0;
		int nrOfFolders = 0;
		for (int i = 0; i < files.length; i++) {
			final FileObject child = files[i];

			boolean isFolder = (child.getType() == FileType.FOLDER);
			listChild(dir, child, showRelativePath, isFolder, longList, engine);

			if (isFolder) {
				nrOfFolders++;
			} else {
				nrOfFiles++;
			}
		}
		engine.println(nrOfFolders + " Folder(s), " + nrOfFiles + " File(s)");
	}

	private void listChild(final FileObject base, final FileObject child,
			boolean showRelativePath, boolean isFolder, boolean longList, Engine engine)
			throws FileSystemException {

		if (!child.exists()) {
			listUnexisting(base, child, showRelativePath, engine);
		} else if (longList && longListType != null && longListType.equals("dos")) {
			SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
			listChildDosStyle(base, child, isFolder, showRelativePath,
					dateFormatter, engine);
		} else if (longList) {
			SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
			listChildNixStyle(base, child, isFolder, showRelativePath,
					dateFormatter, engine);
		} else {
			listChild(base, child, isFolder, showRelativePath, engine);
		}
				
	}

	protected void listUnexisting(final FileObject base,
			final FileObject child, boolean showRelativePath,
			Engine engine) throws FileSystemException {
		engine.println(getPath(base, child, showRelativePath) + " does not exist");
	}
	
	protected void listChild(final FileObject base, final FileObject child,
			boolean isFolder, boolean showRelativePath, Engine engine)
			throws FileSystemException {

		engine.print(getPath(base, child, showRelativePath));
		
		if (isFolder) {
			engine.println("/");
		} else {
			engine.println("");
		}

	}

	protected void listChildDosStyle(final FileObject base,
			final FileObject child, boolean isFolder, boolean showRelativePath,
			DateFormat dateFormatter, Engine engine) throws FileSystemException {

		long timestamp = child.getContent().getLastModifiedTime();
		Date date = new Date(timestamp);

		String middle = "                 ";
		if (isFolder) {
			middle = "   <DIR>         ";
		} else {
			long size = child.getContent().getSize();
			String sizeAsString = Long.toString(size);
			String formattedSize;
			if (sizeAsString.length() <= 3) {
				formattedSize = sizeAsString;
			} else {
				int start = sizeAsString.length() % 3;
				if (start == 0)
					start = 3;
				formattedSize = sizeAsString.substring(0, start);

				for (int i = start; i < sizeAsString.length(); i = i + 3) {
					formattedSize = formattedSize + "."
							+ sizeAsString.substring(i, i + 3);
				}
			}
			middle = middle.substring(0, middle.length()
					- formattedSize.length())
					+ formattedSize;
		}

		engine.print(dateFormatter.format(date) + middle + " ");
		engine.println(getPath(base, child, showRelativePath));

	}

	protected void listChildNixStyle(final FileObject base,
			final FileObject child, boolean isFolder, boolean showRelativePath,
			DateFormat dateFormatter, Engine engine) throws FileSystemException {

		String start;

		if (isFolder) {
			start = "d";
		} else {
			start = "-";
		}

		if (child.isWriteable())
			start += "w";
		else
			start += "-";

		if (child.isReadable())
			start += "r";
		else
			start += "-";

		long timestamp = child.getContent().getLastModifiedTime();
		Date date = new Date(timestamp);

		String middle = "            ";
		if (!isFolder) {
			long size = child.getContent().getSize();
			String sizeAsString = Long.toString(size);
			middle = middle.substring(0, middle.length()
					- sizeAsString.length())
					+ sizeAsString;
		}

		engine.print(start += middle + " " + dateFormatter.format(date) + " ");
		engine.println(getPath(base, child, showRelativePath));
	}

	protected String getPath(FileObject base, FileObject child, boolean showRelativePath) throws FileSystemException {
		if (showRelativePath) {
			return base.getName().getRelativeName(child.getName());
		} else {
			return child.getName().getBaseName();
		}
	}
	
	/**
	 * Set the format the long listing should use.
	 * 
	 * @param longListType
	 *            dos for MS-DOS style, nix for Unix/Linux style
	 */
	public void setLongListType(String longListType) {
		this.longListType = longListType;
	}

	public String getLongListType() {
		return this.longListType;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

}
