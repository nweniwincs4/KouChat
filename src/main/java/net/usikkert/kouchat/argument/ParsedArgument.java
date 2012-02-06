
/***************************************************************************
 *   Copyright 2006-2012 by Christian Ihle                                 *
 *   kontakt@usikkert.net                                                  *
 *                                                                         *
 *   This file is part of KouChat.                                         *
 *                                                                         *
 *   KouChat is free software; you can redistribute it and/or modify       *
 *   it under the terms of the GNU Lesser General Public License as        *
 *   published by the Free Software Foundation, either version 3 of        *
 *   the License, or (at your option) any later version.                   *
 *                                                                         *
 *   KouChat is distributed in the hope that it will be useful,            *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU      *
 *   Lesser General Public License for more details.                       *
 *                                                                         *
 *   You should have received a copy of the GNU Lesser General Public      *
 *   License along with KouChat.                                           *
 *   If not, see <http://www.gnu.org/licenses/>.                           *
 ***************************************************************************/

package net.usikkert.kouchat.argument;

import net.usikkert.kouchat.util.Validate;

/**
 * Contains details about a parsed startup argument.
 *
 * @author Christian Ihle
 */
public class ParsedArgument {

    private final String originalArgument;
    private final Argument argument;

    /**
     * Creates a new instance of a parsed argument.
     *
     * @param originalArgument The unmodified string argument that was parsed.
     * @param argument The result of the parsed argument.
     */
    public ParsedArgument(final String originalArgument, final Argument argument) {
        Validate.notEmpty(originalArgument, "Original argument can not be empty");
        Validate.notNull(argument, "Argument can not be null");

        this.originalArgument = originalArgument;
        this.argument = argument;
    }

    /**
     *  Gets the unmodified string argument that was parsed.
     *
     * @return The unmodified string argument that was parsed.
     */
    public String getOriginalArgument() {
        return originalArgument;
    }

    /**
     * Gets the parsed argument.
     *
     * @return The parsed argument.
     */
    public Argument getArgument() {
        return argument;
    }

    /**
     * Checks if the argument in the parameter is the same as the the parsed argument in this instance.
     *
     * @param anArgument An argument to compare with.
     * @return If the arguments are equal.
     */
    public boolean isEqualTo(final Argument anArgument) {
        Validate.notNull(anArgument, "Argument can not be null");

        return argument.equals(anArgument);
    }
}