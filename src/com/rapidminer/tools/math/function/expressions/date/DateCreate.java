/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2012 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.tools.math.function.expressions.date;

import java.util.GregorianCalendar;
import java.util.Stack;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

/**
 * Adds the current date as a Calendar.
 * 
 * @author Marco Boeck
 */
public class DateCreate extends PostfixMathCommand {
	
	public DateCreate() {
		numberOfParameters = 0;
	}
	
	/**
	 * Creates the Calendar.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run(Stack stack) throws ParseException {
		checkStack(stack);
		
		stack.push(GregorianCalendar.getInstance());
	}
}
