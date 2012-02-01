/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2011 by Rapid-I and the contributors
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
package com.rapidminer.generator;

import com.rapidminer.tools.LogService;

/**
 * This class has one numerical input attribute and one output attribute.
 * Calculates the signum of the input attribute, i.e. -1 for values smaller
 * than 0, 0 for zero, and 1 for values larger than zero.
 * 
 * @author Ingo Mierswa
 *          ingomierswa Exp $
 */
public class SignumGenerator extends SingularNumericalGenerator {

	public SignumGenerator() {}

	@Override
	public FeatureGenerator newInstance() {
		return new SignumGenerator();
	}

	@Override
	public double calculateValue(double value) {
		return Math.signum(value);
	}

	@Override
	public void setFunction(String name) {
		if (!name.equals("sgn"))
			LogService.getGlobal().log("Illegal function name '" + name + "' for " + getClass().getName() + ".", LogService.ERROR);
	}

	@Override
	public String getFunction() {
		return "sgn";
	}
}