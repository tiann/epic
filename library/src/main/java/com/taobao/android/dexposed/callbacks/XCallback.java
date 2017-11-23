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

package com.taobao.android.dexposed.callbacks;

import java.io.Serializable;

import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.DexposedBridge.CopyOnWriteSortedSet;

import android.os.Bundle;

public abstract class XCallback implements Comparable<XCallback> {
	public final int priority;
	public XCallback() {
		this.priority = PRIORITY_DEFAULT;
	}
	public XCallback(int priority) {
		this.priority = priority;
	}
	
	public static class Param {
		public final Object[] callbacks;
		private Bundle extra;
		
		protected Param() {
			callbacks = null;
		}
		
		protected Param(CopyOnWriteSortedSet<? extends XCallback> callbacks) {
			this.callbacks = callbacks.getSnapshot();
		}
		
		/**
		 * This can be used to store anything for the scope of the callback.
		 * Use this instead of instance variables.
		 * @see #getObjectExtra
		 * @see #setObjectExtra
		 */
		public synchronized Bundle getExtra() {
			if (extra == null)
				extra = new Bundle();
			return extra;
		}
		
		/** @see #setObjectExtra */
		public Object getObjectExtra(String key) {
			Serializable o = getExtra().getSerializable(key);
			if (o instanceof SerializeWrapper)
				return ((SerializeWrapper) o).object;
			return null;
		}
		
		/** Provides a wrapper to store <code>Object</code>s in <code>extra</code>. */
		public void setObjectExtra(String key, Object o) {
			getExtra().putSerializable(key, new SerializeWrapper(o));
		}
		
		private static class SerializeWrapper implements Serializable {
			private static final long serialVersionUID = 1L;
			private Object object;
			public SerializeWrapper(Object o) {
				object = o;
			}
		}
	}
	
	public static final void callAll(Param param) {
		if (param.callbacks == null)
			throw new IllegalStateException("This object was not created for use with callAll");
		
		for (int i = 0; i < param.callbacks.length; i++) {
			try {
				((XCallback) param.callbacks[i]).call(param);
			} catch (Throwable t) { DexposedBridge.log(t); }
		}
	}
	
	protected void call(Param param) throws Throwable {};
	
	@Override
	public int compareTo(XCallback other) {
		if (this == other)
			return 0;
		
		// order descending by priority
		if (other.priority != this.priority)
			return other.priority - this.priority;
		// then randomly
		else if (System.identityHashCode(this) < System.identityHashCode(other))
			return -1;
		else
			return 1;
	}
	
	public static final int PRIORITY_DEFAULT = 50;
	/** Call this handler last */
	public static final int PRIORITY_LOWEST = -10000;
	/** Call this handler first */
	public static final int PRIORITY_HIGHEST = 10000;
}
