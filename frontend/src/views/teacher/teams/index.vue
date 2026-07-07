<template>
  <div class="tch-page">
    <div class="tch-filter-bar">
      <div class="tch-search">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <input v-model="searchQuery" type="search" placeholder="搜索组名、选题…" />
      </div>
      <div class="tch-filter-tabs" role="tablist" aria-label="小组状态">
        <button
          v-for="tab in statusTabs"
          :key="tab.id"
          type="button"
          role="tab"
          class="tch-filter-tab"
          :class="{ active: activeStatus === tab.id }"
          :aria-selected="activeStatus === tab.id"
          @click="activeStatus = tab.id"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div class="wb-card tch-panel tch-panel--flush">
      <div class="tch-table-wrap tch-table-wrap--flush">
        <table class="tch-table">
          <thead>
            <tr>
              <th>组名</th>
              <th>选题</th>
              <th>状态</th>
              <th>成员</th>
              <th>任务进度</th>
              <th>最近周报</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="team in filteredTeams" :key="team.id">
              <td><strong>{{ team.teamLabel }}</strong></td>
              <td>{{ team.topicTitle }}</td>
              <td>
                <span class="tch-tag" :class="teamStatusTagClass(team.status)">
                  {{ teamStatusLabel(team.status) }}
                </span>
              </td>
              <td>{{ team.memberCount }}</td>
              <td>
                <div class="wb-progress-wrap">
                  <div class="wb-progress-bar">
                    <div
                      class="wb-progress-fill"
                      :class="progressFillClass(team.progressPercent)"
                      :style="{ width: team.progressPercent != null ? `${team.progressPercent}%` : '0%' }"
                    />
                  </div>
                  <span class="wb-progress-label">
                    {{ team.progressPercent != null ? `${team.progressPercent}%` : '—' }}
                  </span>
                </div>
              </td>
              <td>{{ team.lastWeeklyLabel }}</td>
              <td>
                <RouterLink :to="team.actionTo" class="tch-table-link">{{ team.actionLabel }}</RouterLink>
              </td>
            </tr>
            <tr v-if="!filteredTeams.length">
              <td colspan="7" class="tch-table-empty">没有匹配的小组</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import {
  MOCK_TEAMS,
  TEAM_OVERVIEW_STATUS,
  progressFillClass,
  teamStatusLabel,
  teamStatusTagClass,
} from '../mockTeachingData'

const teams = MOCK_TEAMS
const searchQuery = ref('')
const activeStatus = ref('all')

const statusTabs = computed(() => {
  const all = teams.length
  const active = teams.filter((item) => item.status === TEAM_OVERVIEW_STATUS.ACTIVE).length
  const pending = teams.filter((item) => item.status === TEAM_OVERVIEW_STATUS.PENDING).length
  const atRisk = teams.filter((item) => item.status === TEAM_OVERVIEW_STATUS.AT_RISK).length
  return [
    { id: 'all', label: `全部（${all}）` },
    { id: TEAM_OVERVIEW_STATUS.ACTIVE, label: `在研（${active}）` },
    { id: TEAM_OVERVIEW_STATUS.PENDING, label: `待审（${pending}）` },
    { id: TEAM_OVERVIEW_STATUS.AT_RISK, label: `需关注（${atRisk}）` },
  ]
})

const filteredTeams = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  return teams.filter((item) => {
    const statusMatch = activeStatus.value === 'all' || item.status === activeStatus.value
    if (!statusMatch) return false
    if (!keyword) return true
    return (
      item.teamLabel.toLowerCase().includes(keyword) ||
      item.topicTitle.toLowerCase().includes(keyword)
    )
  })
})
</script>
