/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */
@file:Suppress("NOTHING_TO_INLINE")

package com.mypasswordgen.jsclient.externals

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import react.RefCallback

class AutoAnimateInstance<T : Element> private constructor() {
    inline operator fun component1(): react.MutableRefObject<T> = asDynamic()[0]
    inline operator fun component2(): (Boolean) -> Unit = asDynamic()[1]
}

fun <T : HTMLElement> autoAnimateRefCallBack(options: AutoAnimateOptionsPartial? = null): RefCallback<T> =
    if (options == null) RefCallback { node ->
        if (node != null) autoAnimate(node)
    } else RefCallback { node ->
        if (node != null) autoAnimate(node, options)
    }
