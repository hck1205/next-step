package com.nextstep.app.ui.hub

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.domain.hub.ConcernSection
import com.nextstep.app.domain.hub.HubViewer
import com.nextstep.app.ui.components.input.SegmentedRow
import com.nextstep.app.ui.components.layout.CompactTopBar
import com.nextstep.app.ui.components.layout.LocalSectionAdd
import com.nextstep.app.ui.components.layout.SectionAdd
import com.nextstep.app.ui.components.layout.TitleBackRow
import kotlinx.coroutines.launch

/**
 * 기록 탭(학생은 "나"): 기능을 관심사별로 나누되 한 번에 한 층만 보입니다.
 * 처음은 한눈에(관심사 타일 = 목차). 타일을 누르면 그 관심사로 들어가고, 머리에 "‹ 한눈에" · 관심사 이름 · 질문([TitleBackRow]).
 * 관심사는 페이지라 옆으로 밀어서도 넘깁니다. 순서는 보는 자리가 정합니다(HubAudience).
 * 그 아래 줄 = 그 관심사의 섹션(예: 공부 › 스스로 하는 힘 · 진도 · 공부 시간 · 공부 습관 · 일정). 섹션 하나가 기능 화면 하나이고 각자 ViewModel 을 가집니다.
 * 어떤 섹션이 보이는지는 domain/hub/ConcernSection 이 정합니다(학생은 화면 단계에 따라 줄고, 멘토에게 신체 기록은 없음).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HubScreen(caps: Capabilities, studentLevel: StudentUiLevel?, actions: HubActions, initialSection: ConcernSection) {
    val viewer = remember(caps, studentLevel) { HubViewer.of(caps, studentLevel) }
    val concerns = remember(viewer) { ConcernSection.concernsFor(viewer) }
    val pager = rememberPagerState(initialPage = concerns.indexOf(initialSection.concern).coerceAtLeast(0)) { concerns.size }
    val chosen = remember { mutableStateMapOf(initialSection.concern to initialSection) }
    val scope = rememberCoroutineScope()
    val open: (ConcernSection) -> Unit = { section ->
        val page = concerns.indexOf(section.concern)
        if (page >= 0 && section.visibleFor(viewer)) {
            chosen[section.concern] = section
            scope.launch { pager.animateScrollToPage(page) }
        }
    }
    val openConcern: (Concern) -> Unit = { concern -> concerns.indexOf(concern).takeIf { it >= 0 }?.let { page -> scope.launch { pager.animateScrollToPage(page) } } }
    val sectionOf: (Concern) -> ConcernSection = { concern ->
        val sections = ConcernSection.sectionsOf(concern, viewer)
        chosen[concern]?.takeIf { it in sections } ?: sections.first()
    }
    // 섹션마다 올린 "만들기". 보고 있는 섹션의 것만 상단 바 오른쪽에 보입니다(본문 속 + 버튼 대신).
    val adds = remember { mutableStateMapOf<ConcernSection, SectionAdd>() }
    val currentAdd = concerns.getOrNull(pager.currentPage)?.let { adds[sectionOf(it)] }

    Scaffold(
        topBar = {
            CompactTopBar(
                title = if (caps.isStudent) "나" else "기록",
                actions = { currentAdd?.let { SectionAddButton(it) } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            concerns.getOrNull(pager.currentPage)?.let { HubHeader(it, onIndex = { openConcern(Concern.OVERVIEW) }) }
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize(), key = { concerns[it].name }) { page ->
                val concern = concerns[page]
                val sections = ConcernSection.sectionsOf(concern, viewer)
                val section = sectionOf(concern)
                Column(Modifier.fillMaxSize()) {
                    if (sections.size > 1) {
                        SegmentedRow(
                            options = sections, selected = section, label = { it.label }, onSelect = { chosen[concern] = it },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }
                    val register: (SectionAdd?) -> Unit = remember(section) { { add -> if (add == null) adds.remove(section) else adds.put(section, add) } }
                    CompositionLocalProvider(LocalSectionAdd provides register) {
                        Box(Modifier.fillMaxSize()) { HubSectionContent(section, caps, viewer, concerns, actions, open, openConcern) }
                    }
                }
            }
        }
    }
}

/** 지금 보는 관심사의 머리: 한눈에(목차)면 안내 한 줄, 관심사 안이면 "‹ 한눈에" · 이름 · 질문. */
@Composable
private fun HubHeader(current: Concern, onIndex: () -> Unit) {
    val atIndex = current == Concern.OVERVIEW
    TitleBackRow(
        title = current.label, subtitle = if (atIndex) "관심사를 누르면 그 기록만 펼쳐요" else current.question,
        backLabel = "한눈에", onBack = if (atIndex) null else onIndex,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

/** 보고 있는 섹션이 올린 "만들기"(상단 바 오른쪽). */
@Composable
private fun SectionAddButton(add: SectionAdd) {
    TextButton(onClick = add.onClick) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(add.label)
    }
}
