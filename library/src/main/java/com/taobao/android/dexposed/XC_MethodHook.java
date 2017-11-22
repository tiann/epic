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

import java.lang.reflect.Member;

import com.taobao.android.dexposed.callbacks.IXUnhook;
import com.taobao.android.dexposed.callbacks.XCallback;

public abstract class XC_MethodHook extends XCallback {
	public XC_MethodHook() {
		super();
	}
	public XC_MethodHook(int priority) {
		super(priority);
	}
	
	/**
	 * Called before the invocation of the method.
	 * <p>Can use {@link MethodHookParam#setResult(Object)} and {@link MethodHookParam#setThrowable(Throwable)}
	 * to prevent the original method from being called.
	 */
	protected void beforeHookedMethod(MethodHookParam param) throws Throwable {}
	
	/**
	 * Called after the invocation of the method.
	 * <p>Can use {@link MethodHookParam#setResult(Object)} and {@link MethodHookParam#setThrowable(Throwable)}
	 * to modify the return value of the original method.
	 */
	protected void afterHookedMethod(MethodHookParam param) throws Throwable  {}
	
	
	public static class MethodHookParam extends Param {
		/** Description of the hooked method */
		public Member method;
		/** The <code>this</code> reference for an instance method, or null for static methods */
		public Object thisObject;
		/** Arguments to the method call */
		public Object[] args;
		
		private Object result = null;
		private Throwable throwable = null;
		/* package */ boolean returnEarly = false;
		
		/** Returns the result of the method call */
		public Object getResult() {
			return result;
		}
		
		/**
		 * Modify the result of the method call. In a "before-method-call"
		 * hook, prevents the call to the original method.
		 * You still need to "return" from the hook handler if required.
		 */
		public void setResult(Object result) {
			this.result = result;
			this.throwable = null;
			this.returnEarly = true;
		}
		
		/** Returns the <code>Throwable</code> thrown by the method, or null */
		public Throwable getThrowable() {
			return throwable;
		}
		
		/** Returns true if an exception was thrown by the method */
		public boolean hasThrowable() {
			return throwable != null;
		}
		
		/**
		 * Modify the exception thrown of the method call. In a "before-method-call"
		 * hook, prevents the call to the original method.
		 * You still need to "return" from the hook handler if required.
		 */
		public void setThrowable(Throwable throwable) {
			this.throwable = throwable;
			this.result = null;
			this.returnEarly = true;
		}
		
		/** Returns the result of the method call, or throws the Throwable caused by it */
		public Object getResultOrThrowable() throws Throwable {
			if (throwable != null)
				throw throwable;
			return result;
		}
	}

	public class Unhook implements IXUnhook {
		private final Member hookMethod;

		public Unhook(Member hookMethod) {
			this.hookMethod = hookMethod;
		}
		
		public Member getHookedMethod() {
			return hookMethod;
		}
		
		public XC_MethodHook getCallback() {
			return XC_MethodHook.this;
		}

		@Override
		public void unhook() {
			DexposedBridge.unhookMethod(hookMethod, XC_MethodHook.this);
		}

	}
	
	/**
	 * 
	 * Note: This class used for distinguish the Method cann't unhook.
	 *
	 */	
	public abstract class XC_MethodKeepHook extends XC_MethodHook {
		
		public XC_MethodKeepHook() {
			super();
		}
		public XC_MethodKeepHook(int priority) {
			super(priority);
		}

	}


}


