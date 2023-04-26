package com.weather.composeexample

import android.os.Bundle
import android.util.Log
import android.view.SurfaceControl
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.weather.composeexample.ui.theme.ComposeExampleTheme
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding.root와 같이 view를 넣는 대신 Composable함수를 호출
        setContent {
            ComposeExampleTheme {
                TestApp(Modifier)
            }
        }
    }
}

//코드의 재사용성을 높여주면서 프리뷰에 전체화면이 나오도록 만든다.
//컴포저블 함수는 파스칼 케이스를 써야한다.
@Composable
private fun TestApp(modifier: Modifier = Modifier) {
    //호이스팅 완료/ 끌어올렸다.
    var shouldShowOnboarding by remember { mutableStateOf(true) }
    Surface(
        //Modifier -> 상위 요소 레이 아웃 내에서 UI 요소가 배치및 표시 되고 동작 하는 방식을 UI 요소 에게 알려 줍니다
        modifier = Modifier
    ) {
        if (shouldShowOnboarding) {
            OnboardingScreen({ shouldShowOnboarding = false })
        } else {
            Column(modifier = modifier.padding(vertical = 4.dp)) {
                repeat(2) {
                    Greeting(name = "android")
                }
            }
        }
    }
}

//기본적으로 빈 수정자가 할당되는 변수를 두는게 좋다(권장사항)
//구성 함수(컴포저블 함수) -> 다른 함수를 호출할 수 있다.
@Composable
fun Greeting(name: String = "android", modifier: Modifier = Modifier) {

    //상태를 구독하고 !! 리컴포지션 될 때 리컴포지션 안되도록 기억하고 있어라!! 라는 의미
    val expanded = remember { mutableStateOf(false) }
    val extraPadding = if (expanded.value) 48.dp else 0.dp

    //표면 -> UI관련된 래핑 클래스
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(modifier = Modifier.padding(24.dp)) {
            Column(
                modifier = modifier
                    .weight(1f)
                    .padding(bottom = extraPadding)
            ) {
                //일반 코틀린처럼 함수 사용 가능 -> 컴포저블하기 때문에
                Text(text = "Hello $name!")
                Text(text = name)
            }
            //튀어나온 버튼
            ElevatedButton(onClick = { expanded.value = !expanded.value }) {
                Text(if (expanded.value) "show less" else "show more")
            }
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

//미리보기
@Preview(showBackground = true, widthDp = 400)
@Composable
fun GreetingPreview() {
    ComposeExampleTheme {
        Greeting()
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardPreview() {
    ComposeExampleTheme {
        OnboardingScreen({})
    }
}

@Preview
@Composable
fun MyAppPrevier() {
    ComposeExampleTheme {
        TestApp(Modifier.fillMaxSize())
    }
}