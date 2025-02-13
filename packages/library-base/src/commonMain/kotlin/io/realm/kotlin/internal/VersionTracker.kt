/*
 * Copyright 2022 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.kotlin.internal

import io.realm.kotlin.VersionId
import io.realm.kotlin.internal.interop.RealmInterop
import io.realm.kotlin.internal.interop.RealmPointer
import io.realm.kotlin.internal.platform.WeakReference
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

/**
 * Bookkeeping of intermediate versions that needs to be closed when no longer referenced or when
 * explicitly closing a realm.
 *
 * NOTE: This is not thread safe, so synchronization should be enforced by the owner/caller.
 */
internal class VersionTracker(private val owner: BaseRealmImpl, private val log: ContextLogger) {
    // Set of currently open realms. Storing the native pointer explicitly to enable us to close
    // the realm when the RealmReference is no longer referenced anymore.
    private val intermediateReferences: AtomicRef<Set<Pair<RealmPointer, WeakReference<RealmReference>>>> = atomic(mutableSetOf())

    fun trackAndCloseExpiredReferences(realmReference: FrozenRealmReference? = null) {
        val references = mutableSetOf<Pair<RealmPointer, WeakReference<RealmReference>>>()
        realmReference?.let {
            log.trace("$owner TRACK-VERSION ${realmReference.version()}")
            references.add(Pair(realmReference.dbPointer, WeakReference(it)))
        }
        intermediateReferences.value.forEach { entry ->
            val (pointer, ref) = entry
            if (ref.get() == null) {
                log.trace("$owner CLOSE-FREED ${RealmInterop.realm_get_version_id(pointer)}")
                RealmInterop.realm_close(pointer)
            } else {
                references.add(entry)
            }
        }
        intermediateReferences.value = references
    }

    fun versions(): Set<VersionId> =
        // We could actually also report freed versions here!?
        intermediateReferences.value.mapNotNull { it.second.get()?.version() }.toSet()

    fun close() {
        intermediateReferences.value.forEach { (pointer, _) ->
            log.trace("$owner CLOSE-ACTIVE ${VersionId(RealmInterop.realm_get_version_id(pointer))}")
            RealmInterop.realm_close(pointer)
        }
    }
}
