<template>
  <div class="page">
    <div class="container">
      <!-- 筛选面板 -->
      <div class="filters card">
        <div class="frow">
          <span class="flabel">行业</span>
          <div class="chips">
            <span class="chip" :class="{ on: !query.category }" @click="pick('category', '')">不限</span>
            <span v-for="c in categories" :key="c.name" class="chip"
                  :class="{ on: query.category === c.name }" @click="pick('category', c.name)">
              {{ c.name }}<em>{{ c.count }}</em>
            </span>
          </div>
        </div>

        <div class="frow">
          <span class="flabel">城市</span>
          <div class="chips">
            <span class="chip" :class="{ on: !query.city }" @click="pick('city', '')">不限</span>
            <span v-for="c in shownCities" :key="c.name" class="chip"
                  :class="{ on: query.city === c.name }" @click="pick('city', c.name)">
              {{ c.name }}<em>{{ c.count }}</em>
            </span>
            <span v-if="cities.length > CITY_LIMIT" class="more" @click="cityAll = !cityAll">
              {{ cityAll ? '收起' : `更多 ${cities.length - CITY_LIMIT} 个` }}
            </span>
          </div>
        </div>

        <div class="frow">
          <span class="flabel">经验</span>
          <div class="chips">
            <span class="chip" :class="{ on: !query.experience }" @click="pick('experience', '')">不限</span>
            <span v-for="e in experiences" :key="e" class="chip"
                  :class="{ on: query.experience === e }" @click="pick('experience', e)">{{ e }}</span>
          </div>
        </div>

        <div class="frow">
          <span class="flabel">学历</span>
          <div class="chips">
            <span class="chip" :class="{ on: !query.education }" @click="pick('education', '')">不限</span>
            <span v-for="e in educations" :key="e" class="chip"
                  :class="{ on: query.education === e }" @click="pick('education', e)">{{ e }}</span>
          </div>
        </div>

        <div class="frow">
          <span class="flabel">薪资</span>
          <div class="chips">
            <span v-for="(r, i) in salaryRanges" :key="r.label" class="chip"
                  :class="{ on: salaryIdx === i }" @click="pickSalary(i)">{{ r.label }}</span>
          </div>
        </div>
      </div>

      <!-- 工具行 -->
      <div class="toolbar card">
        <el-input v-model="query.keyword" placeholder="搜索职位 / 公司 / 技能" clearable style="max-width: 280px"
                  @keyup.enter="search" @clear="search" />
        <el-button type="primary" @click="search">搜索</el-button>
        <el-select v-model="query.sort" style="width: 150px" @change="search">
          <el-option label="最新发布" value="new" />
          <el-option label="薪资从高到低" value="salary_desc" />
          <el-option label="薪资从低到高" value="salary_asc" />
        </el-select>
        <el-button text type="primary" @click="reset">清空筛选</el-button>
        <span class="muted" style="margin-left: auto">共 {{ total }} 个岗位</span>
      </div>

      <el-skeleton v-if="loading" :rows="6" animated style="margin-top:20px" />
      <el-empty v-else-if="!list.length" description="没有找到符合条件的岗位，试试放宽筛选条件" />
      <div v-else class="list">
        <div v-for="j in list" :key="j.id" class="jrow card" @click="$router.push(`/job/${j.id}`)">
          <div class="left">
            <div class="jtop">
              <b class="title">{{ j.title }}</b>
              <span class="salary">{{ j.salaryMin }}-{{ j.salaryMax }}K</span>
            </div>
            <div class="meta">
              <el-tag v-if="j.category" size="small" type="success" effect="plain">{{ j.category }}</el-tag>
              <span>{{ j.company }}</span>
              <el-divider direction="vertical" />
              <span>{{ j.city }}</span>
              <el-divider direction="vertical" />
              <span>{{ j.experience }}</span>
              <el-divider direction="vertical" />
              <span>{{ j.education }}</span>
            </div>
            <div class="tags">
              <el-tag v-for="t in splitTags(j.tags)" :key="t" size="small" effect="plain">{{ t }}</el-tag>
            </div>
          </div>
          <div class="right">
            <span class="muted">{{ j.hrName }}</span>
            <span class="muted">{{ (j.createTime || '').replace('T', ' ').slice(0, 16) }}</span>
          </div>
        </div>
      </div>

      <div class="pager" v-if="total > size">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="size"
                       :current-page="page" @current-change="onPage" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api'

const CITY_LIMIT = 10
const route = useRoute()
const router = useRouter()
const size = 9
const page = ref(1)
const total = ref(0)
const list = ref([])
const loading = ref(true)
const categories = ref([])
const cities = ref([])
const cityAll = ref(false)
const experiences = ['在校/应届', '1-3年', '3-5年', '5-10年']
const educations = ['大专', '本科', '硕士', '博士', '不限']
const salaryRanges = [
  { label: '不限', min: null, max: null },
  { label: '5K 以下', min: null, max: 5 },
  { label: '5-10K', min: 5, max: 10 },
  { label: '10-15K', min: 10, max: 15 },
  { label: '15-25K', min: 15, max: 25 },
  { label: '25-50K', min: 25, max: 50 },
  { label: '50K 以上', min: 50, max: null }
]
const salaryIdx = ref(0)

const query = reactive({
  keyword: route.query.keyword || '',
  category: route.query.category || '',
  city: '',
  experience: '',
  education: '',
  salaryMin: null,
  salaryMax: null,
  sort: 'new'
})

const shownCities = computed(() => (cityAll.value ? cities.value : cities.value.slice(0, CITY_LIMIT)))

function splitTags(tags) {
  return (tags || '').split(/[,，]/).filter(Boolean).slice(0, 5)
}

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size, sort: query.sort }
    for (const k of ['keyword', 'category', 'city', 'experience', 'education']) {
      if (query[k]) params[k] = query[k]
    }
    if (query.salaryMin != null) params.salaryMin = query.salaryMin
    if (query.salaryMax != null) params.salaryMax = query.salaryMax
    const data = await api.jobList(params)
    list.value = data.records || []
    total.value = Number(data.total || 0)
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  const q = {}
  if (query.keyword) q.keyword = query.keyword
  if (query.category) q.category = query.category
  router.replace({ path: '/jobs', query: q })
  load()
}

function pick(key, value) {
  query[key] = value
  search()
}

function pickSalary(i) {
  salaryIdx.value = i
  query.salaryMin = salaryRanges[i].min
  query.salaryMax = salaryRanges[i].max
  search()
}

function reset() {
  query.keyword = ''
  query.category = ''
  query.city = ''
  query.experience = ''
  query.education = ''
  query.sort = 'new'
  salaryIdx.value = 0
  query.salaryMin = null
  query.salaryMax = null
  search()
}

function onPage(p) {
  page.value = p
  load()
}

watch(() => route.query, (q) => {
  const kw = q.keyword || ''
  const cat = q.category || ''
  if (kw === query.keyword && cat === query.category) return
  query.keyword = kw
  query.category = cat
  page.value = 1
  load()
})

onMounted(async () => {
  try {
    const s = await api.jobStats()
    categories.value = s?.categories || []
    cities.value = s?.cities || []
  } catch {
    /* 统计失败不影响岗位列表 */
  }
  load()
})
</script>

<style scoped>
.filters { padding: 6px 18px; }
.frow { display: flex; gap: 12px; align-items: flex-start; padding: 8px 0; border-bottom: 1px dashed var(--border); }
.frow:last-child { border-bottom: none; }
.flabel { width: 40px; flex-shrink: 0; color: var(--ink-3); font-size: 13px; line-height: 26px; }
.chips { display: flex; flex-wrap: wrap; gap: 4px 6px; flex: 1; }
.chip {
  padding: 3px 10px; border-radius: 14px; font-size: 13px; color: var(--ink-2);
  cursor: pointer; transition: all .15s; white-space: nowrap;
}
.chip:hover { color: var(--brand); background: #f2fbfb; }
.chip.on { background: var(--brand); color: #fff; }
.chip em { font-style: normal; font-size: 11px; opacity: .65; margin-left: 3px; }
.more { padding: 3px 6px; font-size: 13px; color: var(--brand); cursor: pointer; }

.toolbar { display: flex; gap: 12px; align-items: center; padding: 14px 18px; margin-top: 14px; flex-wrap: wrap; }
.list { display: flex; flex-direction: column; gap: 14px; margin-top: 18px; }
.jrow { display: flex; justify-content: space-between; padding: 18px 20px; cursor: pointer; transition: all .2s; }
.jrow:hover { box-shadow: 0 8px 22px rgba(31,35,41,.1); transform: translateY(-2px); }
.jtop { display: flex; gap: 14px; align-items: baseline; }
.title { font-size: 16px; }
.salary { font-size: 16px; }
.meta { margin: 8px 0; color: var(--ink-2); font-size: 13px; display: flex; align-items: center; flex-wrap: wrap; gap: 2px; }
.tags { display: flex; gap: 6px; flex-wrap: wrap; }
.right { display: flex; flex-direction: column; align-items: flex-end; gap: 6px; flex-shrink: 0; }
.pager { display: flex; justify-content: center; margin-top: 22px; }
</style>
