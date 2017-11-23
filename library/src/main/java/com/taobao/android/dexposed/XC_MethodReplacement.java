/*
 * Original work Copyright (c) 2005-2008, The Android Open Source Project
 * Modified work Copyright (c) 2013, rovo89 and Tungstwenty
 * Modified work Copyright (c) 2015, Alibaba Mobile Infrastructure (Android) Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.taobao.android.dexposed;


public abstract class XC_MethodReplacement extends XC_MethodHook {
	public XC_MethodReplacement() {
		super();
	}
	public XC_MethodReplacement(int priority) {
		super(priority);
	}
	
	@Override
	protected final void beforeHookedMethod(MethodHookParam param) throws Throwable {
		try {
			Object result = replaceHookedMethod(param);
			param.setResult(result);
		} catch (Throwable t) {
			param.setThrowable(t);
		}
	}
	
	protected final void afterHookedMethod(MethodHookParam param) throws Throwable {}
	
	/**
	 * Shortcut for replacing a method completely. Whatever is returned/thrown here is taken
	 * instead of the result of the original method (which will not be called).
	 */
	protected abstract Object replaceHookedMethod(MethodHookParam param) throws Throwable;
	
	public static final XC_MethodReplacement DO_NOTHING = new XC_MethodReplacement(PRIORITY_HIGHEST*2) {
    	@Override
    	protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
    		return null;
    	};
	};
	
	/**
	 * Creates a callback which always returns a specific value
	 */
	public static XC_MethodReplacement returnConstant(final Object result) {
		return returnConstant(PRIORITY_DEFAULT, result);
	}
	
	/**
	 * @see #returnConstant(Object)
	 */
	public static XC_MethodReplacement returnConstant(int priority, final Object result) {
		return new XC_MethodReplacement(priority) {
			@Override
			protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
				return result;
			}
		};
	}
	
	
	/**
	 * 
	 * Note: This class used for distinguish the Method cann't unhook.
	 *
	 */	
	public abstract class XC_MethodKeepReplacement extends XC_MethodReplacement {
		public XC_MethodKeepReplacement() {
			super();
		}
		public XC_MethodKeepReplacement(int priority) {
			super(priority);
		}
	}

}
