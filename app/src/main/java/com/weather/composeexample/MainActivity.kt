package com.weather.composeexample

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather.composeexample.ui.theme.ComposeExampleTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.draggedItem
import org.burnoutcrew.reorderable.move
import org.burnoutcrew.reorderable.rememberReorderState
import org.burnoutcrew.reorderable.reorderable
import java.security.AccessController.getContext
import java.time.format.TextStyle
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding.root와 같이 view를 넣는 대신 Composable함수를 호출
        setContent {
            val a = arrayListOf<String>("a", "b", "c", "d", "e")
            A(list = a){
                fromIndex, toIndex ->   a.move(fromIndex, toIndex)
            }
        }
    }
}

@Composable
private fun DraggableTextLowLevel() {
    Box(modifier = Modifier.fillMaxSize()) {
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }

        Box(
            Modifier
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .background(Color.Blue)
                .size(50.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetY += dragAmount.y
                    }
                }
        )
    }
}




@Composable
fun <T>A(
    list: List<T>,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit
){
    ComposeExampleTheme {
//                TestApp(Modifier)
        //컬럼의 상태 저장
        val lazyListState = rememberLazyListState()
        //드래그 되는 아이템
        var initiallyDraggedElement by remember { mutableStateOf<LazyListItemInfo?>(null) }
        //현재 드래그 중인 아이템의 정보
        val currentElementItemInfo by remember { mutableStateOf<LazyListItemInfo?>(null) }
        //드래그된 아이템의 인덱스
        var currentIndexOfDraggedItem by remember { mutableStateOf<Int?>(null) }
        //드래그 된 거리
        var draggedDistance by remember { mutableStateOf(0f) }
        //범위
        val scope = rememberCoroutineScope()

        var overscrollJob by remember { mutableStateOf<Job?>(null) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                //터치 관련 이벤트 콜백
                .pointerInput(Unit) {
                    //길게 클릭 콜백
                    detectDragGesturesAfterLongPress(
                        onDrag = { change, offset ->
                            //여기서 offSet위치 계산
                            change.consume()
                            draggedDistance += offset.y

//                            initiallyDraggedElement?.let {

//                                Log.d("currentElementItemInfo", currentElementItemInfo.toString()) // -> null

                                initiallyDraggedElement?.let { hovered ->

//offset end가 마지막인가????
                                    val startOffset = hovered.offset + draggedDistance
//                                    val endOffset = hovered.offset + draggedDistance

//                                    Log.d("toString", "$startOffset, $endOffset")
                                    lazyListState.layoutInfo.visibleItemsInfo
                                        //조건이 아닌 경우 !filter
                                        //길게 터치한 항목 만 터치되도록 필터링
                                        .filter { item ->
                                            Log.d("offsets", "${item.offset} ${startOffset.toInt()}")
                                            item.offset == startOffset.toInt()// || item.offset > startOffset
                                        }
                                        //filter 후 first
                                        //드래그할 아이템 지정
                                        .firstOrNull { item ->
                                            val delta = startOffset - hovered.offset
                                            val result = when {
                                                draggedDistance > 0 -> (startOffset > item.offset)
                                                else -> (startOffset < item.offset)
                                            }
                                            Log.d("result", result.toString())
                                            result
                                        }?.apply {
//                                            Log.d("index", this.index.toString())
                                            currentIndexOfDraggedItem?.let { current ->
                                                onMove.invoke(current, this.index)
                                            }
                                            currentIndexOfDraggedItem = this.index
                                        }
//                                }
                            }
                            //오버 스크롤 시 원래 위치로
                            if (overscrollJob?.isActive == true)
                                return@detectDragGesturesAfterLongPress
//                            initiallyDraggedElement
//                                ?.let {
//                                    val startOffset = it.offset + draggedDistance
//                                    val endOffset = it.size + draggedDistance
//                                    val viewPortStart = lazyListState.layoutInfo.viewportStartOffset
//                                    val viewPortEnd = lazyListState.layoutInfo.viewportEndOffset
//
//                                    when {
//                                        draggedDistance > 0 -> (endOffset - viewPortEnd).takeIf { diff -> diff > 0 }
//                                        draggedDistance < 0 -> (startOffset - viewPortStart).takeIf { diff -> diff < 0 }
//                                        else -> null
//                                    }
//                                }?.let { offset ->
//                                    overscrollJob = scope.launch { lazyListState.scrollBy(offset) }
//                                } ?: run { overscrollJob?.cancel() }
                        },
                        onDragStart = { offset ->
                            lazyListState.layoutInfo.visibleItemsInfo
                                .lastOrNull() { item ->
                                    offset.y.toInt() >= item.offset
                                }
                                ?.also {
                                    //드래그 되는 아이템의 Index 저장
                                    currentIndexOfDraggedItem = it.index
                                    //실질적으로 드래그 되는 항목 저장
                                    initiallyDraggedElement = it
                                }
                        },
                        onDragCancel = {
                            draggedDistance = 0f
                            currentIndexOfDraggedItem = null
                            initiallyDraggedElement = null
                            overscrollJob?.cancel()
                        },
                        onDragEnd = {
                            draggedDistance = 0f
                            currentIndexOfDraggedItem = null
                            initiallyDraggedElement = null
                            overscrollJob?.cancel()
                        }
                    )
                },
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(5.dp)
        ){
            itemsIndexed(list){index, item ->
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .graphicsLayer {
                        //takeIf = 조건문이 참이면 자기자신을 반환함
                        translationY =
                            draggedDistance.takeIf { index == currentIndexOfDraggedItem } ?: 0f
                    }
                    .background(color = Color.Gray)
                , contentAlignment = Alignment.Center
                ){
                    Text(text = item.toString(), modifier = Modifier)
                }
            }
        }
    }
}

//코드의 재사용성을 높여주면서 프리뷰에 전체화면이 나오도록 만든다.
//컴포저블 함수는 파스칼 케이스를 써야한다. - 리턴있으면 카멜
@Composable
private fun TestApp(modifier: Modifier = Modifier) {
    //호이스팅 완료/ 끌어올렸다.
//    var shouldShowOnboarding by remember { mutableStateOf(true) }
    //회전해도 상태를 유지
    var shouldShowOnboarding by rememberSaveable { mutableStateOf(true) }
    Surface(
        //Modifier -> 상위 요소 레이 아웃 내에서 UI 요소가 배치및 표시 되고 동작 하는 방식을 UI 요소 에게 알려 줍니다
        modifier = Modifier
    ) {
        if (shouldShowOnboarding) {
            //변수가 바뀌는 시점 = 클릭 -> 람다로 전달 할 수 있다.
            OnboardingScreen({ shouldShowOnboarding = false })
        } else {
            Greetings(modifier = Modifier)
        }
    }
}

@Composable
fun OnboardingScreen(onContinueClicked: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to Basics Codelab!")
        Button(
            modifier = modifier.padding(vertical = 24.dp),
            onClick = onContinueClicked //이벤트를 동일 시 하게 만듬
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun Greetings(modifier: Modifier, names: List<String> = List(1000) { "$it" }) {
    //LazyColumn하나면 RecyclerView를 만들 수 있다.
    LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
        items(items = names) { name ->
            Greeting(name = name)
        }
    }
}

//기본적으로 빈 수정자가 할당되는 변수를 두는게 좋다(권장사항)
//구성 함수(컴포저블 함수) -> 다른 함수를 호출할 수 있다.
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    //상태를 구독하고 !! 리컴포지션 될 때 리컴포지션 안되도록 기억하고 있어라!! 라는 의미
    val expanded = remember { mutableStateOf(false) }
//    val extraPadding = if (expanded.value) 48.dp else 0.dp
    //애니메이션
    val extraPadding by animateDpAsState(if (expanded.value) 48.dp else 0.dp)

    //표면 -> UI관련된 래핑 클래스
//    Surface(
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )

        ) {
            Column(
                modifier = modifier
                    .weight(1f)
//                    .padding(bottom = extraPadding)
                    .padding(bottom = extraPadding.coerceAtLeast(0.dp))
            ) {
                //일반 코틀린처럼 함수 사용 가능 -> 컴포저블하기 때문에
                Text(text = "Hello")
                Text(
                    text = name, style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                if (expanded.value) {
                    Text(
                        text = ("Composem ipsum mcolor sit lazy, " +
                                "padding theme elit, sed do bouncy").repeat(2)
                    )
                }
            }
            //튀어나온 버튼
//            ElevatedButton(onClick = { expanded.value = !expanded.value }) {
//                Text(if (expanded.value) "show less" else "show more")
//            }
            //이미지로 변경
            IconButton(onClick = { expanded.value = !expanded.value }) {
                Icon(
                    imageVector = if (expanded.value) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded.value) {
                        "show less"
                    } else {
                        "show more"
                    }
                )
            }
        }
    }
}

//미리보기
@Preview(showBackground = true, widthDp = 400, uiMode = UI_MODE_NIGHT_YES, name = "Dark")
@Preview(showBackground = true, widthDp = 400)
@Composable
fun GreetingPreview() {
    ComposeExampleTheme {
        Greetings(Modifier)
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardPreview() {
    ComposeExampleTheme {
        OnboardingScreen({})
    }
}

@Preview(showSystemUi = true)
@Composable
fun MyAppPrevier() {
    ComposeExampleTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = """
                    ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd
                """.trimIndent(),
                modifier = Modifier
                    .height(100.dp)
                    .background(color = Color.Black),
                color = Color.Red,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Visible,
                softWrap = true,
                style = MaterialTheme.typography.displayLarge
            )
        }
    }
}
