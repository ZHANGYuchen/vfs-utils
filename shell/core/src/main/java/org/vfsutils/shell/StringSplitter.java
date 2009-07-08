package org.vfsutils.shell;

import java.util.ArrayList;
import java.util.List;

public class StringSplitter {
	
	private char[] delimiterChars = new char[] {' ', '\n'};
	private char[] quoteChars = new char[] {'\'', '"'};
	private char[] escapeChars = new char[] {'\\'};
	
	public void setDelimiterChars(char[] chars) {
		this.delimiterChars = chars;
	}
	
	public void setQuoteChars(char[] chars) {
		this.quoteChars = chars;
	}
	
	public void setEscapeChars(char[] chars) {
		this.escapeChars = chars;
	}
	
	protected char[] joinDelimitersAndQuotes() {
		char[] joined = new char[this.delimiterChars.length + this.quoteChars.length];
		System.arraycopy(this.delimiterChars, 0, joined, 0, this.delimiterChars.length);
		System.arraycopy(this.quoteChars, 0, joined, this.delimiterChars.length, this.quoteChars.length);
		return joined;
	}
	
	/**
	 * Splits a line in tokens at whitespace boundaries. Whitespace can be escaped by a backslash and 
	 * quoted blocks (single or double quotes) to avoid a split.
	 * @param line
	 * @return the tokens after the split at whitespace
	 */
	public String[] split(String line) {
		
		List parts = new ArrayList();
		
		char[] chars = line.toCharArray();
		
		StringBuffer part = new StringBuffer();
		
		boolean escaped = false;
		boolean quoted = false;
		//initialized on dummy value
		char quoteChar = '0';
		
		char c;
		for (int i=0; i < chars.length; i++) {
			c = chars[i];
			//reset escape
			escaped = false;
			
			//if escaped, skip escape character
			if (c=='\\') {
				escaped = true;
				//skip to next
				i++;
				//stop if at end
				if (i>=chars.length) break;
				//reassign c
				c = chars[i];
			}
			
			if (c=='"' || c=='\'') {
				if (escaped) {
					part.append(c);
				}
				else if (quoted && quoteChar==c) {
					quoted = false;
				}
				else if (quoted) {
					part.append(c);
				}
				else {
					quoted=true;
					quoteChar=c;
				}
			}
			else if (c==' ' || c=='\n') {
				if (escaped || quoted) {
					part.append(c);
				}
				else {
					//when space (and not escaped) split
					addPart(parts, part);
				}
			}
			else {
				part.append(c);
			}
		}
		
		//treat left-overs
		if (part.length()>0) {
			addPart(parts, part);
		}
		
		String[] result = new String[parts.size()];
		return (String[]) parts.toArray(result);
	}

	/**
	 * Removes all non-significant quotes and escape characters. It gives the
	 * same result as the split would except that it delimiters are not removed.
	 * Once the quotes and escapes are removed the input can not be split anymore.
	 * @param input
	 * @return
	 */
	public String removeQuotesAndEscapes(String input) {
		char[] chars = input.toCharArray();
		
		StringBuffer part = new StringBuffer();
		
		boolean escaped = false;
		boolean quoted = false;
		//initialized on dummy value
		char quoteChar = '0';
		
		char c;
		for (int i=0; i < chars.length; i++) {
			c = chars[i];
			
			if (isEscape(c)) {
				if (escaped) {
					//escaped escape
					escaped = false;
					part.append(c);
				}
				else {
					escaped = true;
				}
			}
			else if (isQuote(c)) {
				if (escaped) {
					//reset escape
					escaped = false;
					part.append(c);					
				}
				else if (quoted && quoteChar==c) {
					quoted = false;
				}
				else if (quoted) {
					part.append(c);
				}
				else {
					quoted=true;
					quoteChar=c;
				}
			}
			else if (isDelimiter(c)) {
				if (escaped || quoted) {
					escaped = false;
					part.append(c);
				}
				else {
					//should never get here because split is 
					//done before
					part.append(c);
				}
			}
			else {
				escaped=false;
				part.append(c);
			}			
		}
		return part.toString();
	}
	
	/**
	 * Adds the content of the buffer to the list of parts
	 * and empties the buffer. 
	 * @param parts
	 * @param part
	 */
	protected void addPart(List parts, StringBuffer part) {
		
		if (part.length()==0) return;
		
		parts.add(part.toString());
		part.delete(0, part.length());
	}
	
	protected boolean isDelimiter(char c) {
		return isCharIn(delimiterChars, c);
	}
	
	protected boolean isQuote(char c) {
		return isCharIn(quoteChars, c);
	}
	
	protected boolean isEscape(char c) {
		return isCharIn(escapeChars, c);
	}
	
	private boolean isCharIn(char[] chars, char c) {
		for (int i=0; i<chars.length; i++) {
			if (chars[i]==c) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Escapes whitespace, single and double quotes by prepending a backslash
	 * @param input
	 * @return
	 */
	public String escapeWhitespaceAndQuotes(String input) {
		return escape(input, new char[] {' ', '\'', '"' });
	}
		
	/**
	 * Escapes the given characters by prepending a backslash
	 * @param input
	 * @param matches
	 * @return
	 */
	protected String escape(String input, char[] matches) {
		StringBuffer buffer = new StringBuffer(input.length()+5);
		char[] chars = input.toCharArray();
		
		for (int i=0; i < chars.length; i++) {
			char c = chars[i];
			for (int j=0; j<matches.length; j++) {
				if (c==matches[j]) {
					buffer.append('\\');
				}
			}			
			buffer.append(c);
		}
		
		return buffer.toString();

	}
	
	/**
	 * Unescapes whitespace, single and double quotes by removing the
	 * prepending backslash 
	 * @param input
	 * @return
	 */
	public String unescapeWhiteSpaceAndQuotes(String input) {
		return unescape(input, new char[] {' ', '\'', '"' }); 
	}
	
	/**
	 * Unescapes the given characters by removing the prepending backslash
	 * @param input
	 * @param matches
	 * @return
	 */
	protected String unescape(String input, char[] matches) {
		StringBuffer buffer = new StringBuffer(input.length());
		char[] chars = input.toCharArray();
		
		charloop: for (int i=0; i < chars.length; i++) {
			char c = chars[i];
			if (c=='\\') {
				//just hope no one escapes 0
				char next = (i==(chars.length-1)?'0':chars[i+1]);
				for (int j=0; j<matches.length; j++) {
					if (next==matches[j]) {
						continue charloop;
						
					}
				}				
			}			
			buffer.append(c);
		}
		
		return buffer.toString();

	}
	
	
	

}
