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

package io.realm.kotlin.test.shared

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.test.platform.PlatformUtils
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.linkingObjects
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KMutableProperty1
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFails

class Parent : RealmObject {
    var child: Child? = null
    var anotherChild: Child? = null
    var childs: RealmList<Child>? = null
    var child2: String? = null
}

var x: KMutableProperty1<Parent, Child?> = Parent::child
val y: KMutableProperty1<Parent, Child?> = Parent::anotherChild

class Child : RealmObject {
    // should only allow definitions by input parameters, not external variables
    val parents: RealmResults<Parent> by linkingObjects(x)
    val parentsX: RealmResults<Parent> by linkingObjects(y)
    val parents2: RealmResults<Parent> by linkingObjects(Parent::child2)
}

class LinkingObjectsTests {
    private lateinit var realm: Realm
    private lateinit var tmpDir: String

    @BeforeTest
    fun setup() {
        tmpDir = PlatformUtils.createTempDir()
        val configuration = RealmConfiguration.Builder(setOf(Parent::class, Child::class))
            .directory(tmpDir)
            .build()

        realm = Realm.open(configuration)
    }

    @Test
    fun unmanaged_throw() {
        val a = Child()
        assertFails {
            a.parents
        }
    }

    @Test
    fun managed_works() {
        runBlocking {
            val child = realm.write {
                this.copyToRealm(Child())
            }

            child.parents
        }
    }
}
