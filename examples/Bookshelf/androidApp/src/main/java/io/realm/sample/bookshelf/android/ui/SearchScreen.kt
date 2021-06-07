/*
 * Copyright 2021 Realm Inc.
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
package io.realm.sample.bookshelf.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.realm.sample.bookshelf.model.Book
import kotlinx.coroutines.flow.MutableStateFlow

@ExperimentalComposeUiApi
@Composable
fun searchScreen(
    items: List<Book>,
    searching: MutableStateFlow<Boolean>,
    navController: NavHostController,
    findBooks: (name: String) -> Unit,
    addBook: (Book) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column {
        TodoItemEntryInput {
            keyboardController?.hide()
            findBooks(it)
        }
        val isSearching: Boolean by searching.collectAsState()

        if (isSearching) {
            // Indeterminate
            Column(horizontalAlignment = CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(32.dp))
                Text("🔎 Openlibrary.org...", Modifier.padding(8.dp))
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(top = 8.dp)
            ) {
                items(items = items) { book ->
                    Row {
                        Text(book.title)
                        Button(onClick = {
                            addBook(book)
                            //navigate to My Books View
                            navController.navigate(NavigationScreens.Books.name)
                        }) {
                            Text(text = "➕")
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun TodoItemEntryInput(onItemComplete: (String) -> Unit) {
    val (text, onTextChange) = rememberSaveable { mutableStateOf("") }

    val submit = {
        if (text.isNotBlank()) {
            onItemComplete(text)
            onTextChange("")
        }
    }

    TodoItemInput(
        text = text,
        onTextChange = onTextChange,
        submit = submit,
    ) {
        TodoEditButton(onClick = submit, text = "Find", enabled = text.isNotBlank())
    }
}

@Composable
private fun TodoItemInput(
    text: String,
    onTextChange: (String) -> Unit,
    submit: () -> Unit,
    buttonSlot: @Composable () -> Unit,
) {
    Column {
        Row(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .height(IntrinsicSize.Min)
        ) {
            TodoInputText(
                text = text,
                onTextChange = onTextChange,
                onImeAction = submit,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(Modifier.align(Alignment.CenterVertically)) { buttonSlot() }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun TodoEditButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        shape = CircleShape,
        enabled = enabled,
        modifier = modifier
    ) {
        Text(text)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TodoInputText(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onImeAction: () -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    TextField(
        value = text,
        onValueChange = onTextChange,
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
        maxLines = 1,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            onImeAction()
            keyboardController?.hide()
        }),
        modifier = modifier
    )
}
