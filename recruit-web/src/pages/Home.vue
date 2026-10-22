<template>
  <div class="home">
    <!-- 首屏搜索 -->
    <section class="hero">
      <div class="container hero-inner">
        <h1>找工作，直接和招聘官谈</h1>
        <p class="sub">覆盖互联网、金融、教育、医疗、制造等 {{ categories.length }} 大行业 · {{ total }} 个在招岗位</p>
        <div class="search">
          <el-select v-model="city" class="citysel" size="large" filterable placeholder="城市">
            <el-option v-for="c in cities" :key="c.name" :label="c.name" :value="c.name" />
          </el-select>
          <el-input v-model="keyword" size="large" placeholder="搜索职位、公司或技能，如：Java / 护士 / 会计"
                    @keyup.enter="goSearch" />
          <el-button type="primary" size="large" class="sbtn" @click="goSearch">搜索</el-button>
        </div>
        <div class="hotwords">
          <span>热门职位：</span>
          <a v-for="h in hotWords" :key="h" @click="quickSearch(h)">{{ h }}</a>
        </div>
      </div>
    </section>

    <div class="container page">
      <!-- 行业分类导航 -->
      <div class="catnav">
        <div class="catblock card">
          <div class="catrows">
            <div v-for="c in pageCategories" :key="c.name" class="catrow" :class="{ on: activeCat === c.name }"
                 @click="activeCat = c.name">
              <span class="catname">{{ c.name }}</span>
              <div class="catfuncs">
                <span v-for="t in topTags(c)" :key="t" class="catfunc" @click.stop="quickSearch(t)">{{ t }}</span>
              </div>
              <span class="catarrow" @click.stop="goCat(c)">›</span>
            </div>
          </div>
          <div class="catpager">
            <span>{{ catPage + 1 }} / {{ catTotalPages }}</span>
            <div class="parrows">
              <span class="parrow" :class="{ off: catPage === 0 }" @click="changeCatPage(-1)">‹</span>
              <span class="parrow" :class="{ off: catPage >= catTotalPages - 1 }" @click="changeCatPage(1)">›</span>
            </div>
          </div>
        </div>

        <div class="catpanel card">
          <div class="phead">{{ activeCat }}</div>
          <div class="pgroups">
            <div v-for="g in activeGroups" :key="g.name" class="pgroup">
              <span class="gname">{{ g.name }}</span>
              <div class="gitems">
                <span v-for="t in g.items" :key="t" class="gitem" @click="quickSearch(t)">{{ t }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 热招职位 -->
      <div class="head">
        <h2 class="page-title">热招职位</h2>
        <router-link to="/jobs" class="more">查看更多 →</router-link>
      </div>
      <div class="tabs">
        <span v-for="c in tabCats" :key="c" class="tab" :class="{ on: tabCat === c }" @click="switchTab(c)">{{ c }}</span>
      </div>
      <el-skeleton v-if="loading" :rows="4" animated />
      <el-empty v-else-if="!jobs.length" description="该分类暂无岗位，换个分类看看" />
      <div v-else class="grid">
        <div v-for="j in jobs" :key="j.id" class="jcard card" @click="$router.push(`/job/${j.id}`)">
          <div class="jtop">
            <b class="title">{{ j.title }}</b>
            <span class="salary">{{ j.salaryMin }}-{{ j.salaryMax }}K</span>
          </div>
          <div class="meta">
            <span>{{ j.city }}</span>
            <el-divider direction="vertical" />
            <span>{{ j.experience }}</span>
            <el-divider direction="vertical" />
            <span>{{ j.education }}</span>
          </div>
          <div class="tags">
            <el-tag v-for="t in splitTags(j.tags)" :key="t" size="small" effect="plain">{{ t }}</el-tag>
          </div>
          <div class="jfoot">
            <div class="company">
              <b>{{ j.company }}</b>
              <span class="muted">{{ j.category }}</span>
            </div>
            <span class="muted">{{ j.hrName }}</span>
          </div>
        </div>
      </div>

      <!-- 热门企业 -->
      <div class="head" style="margin-top: 32px">
        <h2 class="page-title">热门企业</h2>
        <router-link to="/jobs" class="more">全部职位 →</router-link>
      </div>
      <div class="comgrid">
        <div v-for="co in companies" :key="co.name" class="comcard card">
          <div class="comhead">
            <div class="comlogo">{{ co.name.slice(0, 1) }}</div>
            <div class="commeta">
              <b>{{ co.name }}</b>
              <span class="muted">{{ co.category }} · {{ co.city }} · {{ co.count }} 个在招职位</span>
            </div>
          </div>
          <div class="cojobs">
            <router-link v-for="j in co.jobs" :key="j.id" :to="`/job/${j.id}`" class="cojob">
              <span class="cotitle">{{ j.title }}</span>
              <span class="salary">{{ j.salaryMin }}-{{ j.salaryMax }}K</span>
            </router-link>
          </div>
          <router-link :to="{ path: '/jobs', query: { keyword: co.name } }" class="morelink">
            查看更多职位 →
          </router-link>
        </div>
      </div>

      <!-- 加入我们 -->
      <div class="join card">
        <div class="joininfo">
          <h2>加入我们</h2>
          <p>寻找有想法的同行者，和一群认真的人把一件事做成。</p>
          <ul class="joinlist">
            <li><span>📋</span>在招岗位 {{ total }} 个 · 覆盖 {{ categories.length }} 大行业</li>
            <li><span>🏙️</span>工作地点：武汉 · 全国 {{ cities.length }} 个城市</li>
            <li><span>📮</span>投递邮箱：hr@example.com</li>
          </ul>
          <div class="joinbtns">
            <el-button type="warning" @click="$router.push('/jobs')">立即投递</el-button>
            <el-button plain @click="$router.push('/match')">AI 帮我选岗位</el-button>
          </div>
        </div>
        <div class="joingallery">
          <div v-for="p in posters" :key="p.img" class="joinitem" @click="$router.push(p.link)">
            <img :src="p.img" :alt="p.title" />
          </div>
        </div>
      </div>

      <!-- 平台服务 -->
      <div class="service card">
        <div v-for="f in features" :key="f.title" class="sitem">
          <div class="sicon">{{ f.icon }}</div>
          <div>
            <b>{{ f.title }}</b>
            <p>{{ f.desc }}</p>
          </div>
        </div>
        <div class="ai-entry" @click="$router.push('/match')">
          <div>
            <b>🤖 不确定投什么？</b>
            <p>让 AI 求职助手按你的简历和期望推荐岗位</p>
          </div>
          <el-button type="primary" plain size="small">去咨询</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { jobCategories, CAT_PAGE_SIZE } from '../data/jobCategories'

const router = useRouter()
const city = ref('武汉')
const keyword = ref('')
const total = ref(0)
const jobs = ref([])
const categories = ref([])
const cities = ref([])
const companies = ref([])
const catPage = ref(0)
const activeCat = ref(jobCategories[0].name)
const tabCat = ref('全部')
const loading = ref(true)
const posters = [
  { img: '/images/banner-1.jpeg', title: '加入我们', link: { path: '/jobs' } },
  {
    img: '/images/banner-2.jpeg',
    title: '加入未来团队',
    link: { path: '/jobs', query: { category: '互联网/AI' } }
  },
  {
    img: '/images/banner-3.jpeg',
    title: '一起做有趣的事',
    link: { path: '/jobs', query: { category: '设计' } }
  }
]
const hotWords = ['Java', '前端', '产品经理', '会计', '护士', '教师', '销售', '设计师']
const features = [
  { icon: '⚡', title: '直接沟通', desc: '与招聘官在线聊，跳过等待' },
  { icon: '🎯', title: '智能匹配', desc: '按技能城市薪资精准推荐' },
  { icon: '📄', title: '一键投递', desc: '在线维护简历快速投递' }
]

const catTotalPages = computed(() => Math.max(1, Math.ceil(jobCategories.length / CAT_PAGE_SIZE)))
const pageCategories = computed(() =>
  jobCategories.slice(catPage.value * CAT_PAGE_SIZE, (catPage.value + 1) * CAT_PAGE_SIZE)
)
const activeGroups = computed(() => {
  const cur = jobCategories.find((c) => c.name === activeCat.value)
  return (cur && cur.groups) || []
})

const tabCats = computed(() => ['全部', ...categories.value.slice(0, 9).map((c) => c.name)])

/** 左侧行业行内展示的代表职位（取第一个分组的 3 个） */
function topTags(c) {
  const first = c.groups && c.groups[0]
  return first ? first.items.slice(0, 3) : []
}

/** 行业行右箭头：跳到该行业的职位列表 */
function goCat(c) {
  router.push({ path: '/jobs', query: { category: c.name } })
}

function splitTags(tags) {
  return (tags || '').split(/[,，]/).filter(Boolean).slice(0, 4)
}

async function loadJobs() {
  loading.value = true
  try {
    const params = { page: 1, size: 6 }
    if (tabCat.value !== '全部') params.category = tabCat.value
    const data = await api.jobList(params)
    jobs.value = data.records || []
  } finally {
    loading.value = false
  }
}

function changeCatPage(step) {
  const next = catPage.value + step
  if (next < 0 || next >= catTotalPages.value) return
  catPage.value = next
}

function switchTab(name) {
  tabCat.value = name
  loadJobs()
}

function goSearch() {
  const q = {}
  if (keyword.value) q.keyword = keyword.value
  if (city.value && city.value !== '武汉') q.city = city.value
  router.push({ path: '/jobs', query: q })
}

function quickSearch(word) {
  keyword.value = word
  goSearch()
}

onMounted(async () => {
  const [stats, comps, first] = await Promise.all([
    api.jobStats().catch(() => null),
    api.jobCompanies(9).catch(() => []),
    api.jobList({ page: 1, size: 6 }).catch(() => null)
  ])
  if (stats) {
    total.value = stats.total || 0
    categories.value = stats.categories || []
    cities.value = stats.cities || []
  }
  companies.value = comps || []
  jobs.value = first?.records || []
  loading.value = false
})
</script>

<style scoped>
/* 首屏 */
.hero { background: linear-gradient(120deg, #e6fafa 0%, #f2fbfb 55%, #ffffff 100%); padding: 52px 0 34px; }
.hero-inner h1 { margin: 0 0 10px; font-size: 34px; letter-spacing: 1px; }
.hero-inner .sub { margin: 0 0 24px; color: var(--ink-2); }
.search { display: flex; gap: 10px; max-width: 760px; }
.citysel { width: 130px; flex-shrink: 0; }
.sbtn { flex-shrink: 0; padding: 0 28px; }
.hotwords { margin-top: 14px; font-size: 12px; color: var(--ink-3); display: flex; gap: 14px; flex-wrap: wrap; }
.hotwords a { cursor: pointer; }
.hotwords a:hover { color: var(--brand); }

/* 行业分类导航：左侧行业列表，右侧二级职能分类 */
.catnav { display: flex; gap: 16px; align-items: stretch; min-height: 424px; }
.catblock { flex: 0 0 420px; display: flex; flex-direction: column; padding: 6px 0; min-width: 0; }
.catrows { flex: 1; display: flex; flex-direction: column; }
.catrow {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 16px; cursor: pointer; transition: background .15s;
}
.catrow:hover { background: #f7fbfb; }
.catrow.on { background: #f2fbfb; }
.catname { width: 96px; flex-shrink: 0; font-size: 14px; color: var(--ink); }
.catrow:hover .catname, .catrow.on .catname { color: var(--brand); }
.catrow.on .catname { font-weight: 600; }
.catfuncs { flex: 1; display: flex; gap: 12px; overflow: hidden; }
.catfunc { font-size: 12px; color: var(--ink-3); white-space: nowrap; cursor: pointer; }
.catfunc:hover { color: var(--brand); text-decoration: underline; }
.catarrow { flex-shrink: 0; color: var(--ink-3); font-size: 16px; line-height: 1; }
.catrow:hover .catarrow, .catrow.on .catarrow { color: var(--brand); }

.catpager {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 16px 6px; border-top: 1px solid var(--border); margin-top: 4px;
}
.catpager > span { font-size: 12px; color: var(--ink-3); }
.parrows { display: flex; gap: 8px; }
.parrow {
  width: 24px; height: 24px; border-radius: 6px; background: #e6fafa; color: var(--brand);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; font-size: 15px; line-height: 1; user-select: none;
}
.parrow:hover { background: var(--brand); color: #fff; }
.parrow.off { background: #f2f3f5; color: #c9cdd4; cursor: not-allowed; }

/* 右侧：二级职能分类面板 */
.catpanel { flex: 1; min-width: 0; padding: 16px 22px; max-height: 424px; overflow-y: auto; }
.phead { font-size: 15px; font-weight: 600; color: var(--ink); margin-bottom: 8px; }
.pgroup {
  display: flex; gap: 14px; align-items: flex-start;
  padding: 8px 0; border-bottom: 1px dashed var(--border);
}
.pgroup:last-child { border-bottom: none; }
.gname { width: 104px; flex-shrink: 0; font-size: 13px; color: var(--ink-2); line-height: 26px; }
.gitems { flex: 1; display: flex; flex-wrap: wrap; gap: 0 16px; }
.gitem { font-size: 13px; color: var(--ink-3); line-height: 26px; white-space: nowrap; cursor: pointer; }
.gitem:hover { color: var(--brand); text-decoration: underline; }

/* 区块标题 */
.head { display: flex; align-items: center; justify-content: space-between; margin-top: 26px; }
.head .page-title { margin: 0; }
.more { color: var(--brand); font-size: 13px; }

/* 热招职位 */
.tabs { display: flex; gap: 8px; flex-wrap: wrap; margin: 14px 0 16px; }
.tab {
  padding: 5px 14px; border-radius: 16px; font-size: 13px; color: var(--ink-2);
  background: #fff; cursor: pointer; border: 1px solid var(--border); transition: all .15s;
}
.tab:hover { color: var(--brand); border-color: #cdeeed; }
.tab.on { background: var(--brand); color: #fff; border-color: var(--brand); }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(330px, 1fr)); gap: 16px; }
.jcard { padding: 18px; cursor: pointer; transition: all .2s; }
.jcard:hover { transform: translateY(-3px); box-shadow: 0 8px 22px rgba(31, 35, 41, .1); }
.jtop { display: flex; justify-content: space-between; align-items: baseline; gap: 10px; }
.title { font-size: 16px; }
.salary { font-size: 16px; }
.meta { margin: 8px 0; color: var(--ink-2); font-size: 13px; display: flex; align-items: center; flex-wrap: wrap; }
.tags { display: flex; gap: 6px; flex-wrap: wrap; min-height: 24px; }
.jfoot {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 12px; padding-top: 10px; border-top: 1px dashed var(--border);
}
.company { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.company b { font-weight: 600; color: var(--ink-2); }

/* 热门企业 */
.comgrid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 16px; margin-top: 4px; }
.comcard { padding: 16px 18px; display: flex; flex-direction: column; gap: 12px; }
.comhead { display: flex; align-items: center; gap: 12px; }
.comlogo {
  width: 40px; height: 40px; border-radius: 10px; background: #e6fafa; color: var(--brand);
  display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 18px; flex-shrink: 0;
}
.commeta { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.commeta b { font-size: 15px; }
.cojobs { display: flex; flex-direction: column; gap: 8px; }
.cojob {
  display: flex; justify-content: space-between; align-items: baseline; gap: 10px;
  padding: 6px 10px; border-radius: 8px; background: #f7f9fb; font-size: 13px;
}
.cojob:hover { background: #f2fbfb; }
.cojob:hover .cotitle { color: var(--brand); }
.cotitle { color: var(--ink-2); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.morelink { font-size: 12px; color: var(--brand); }

/* 加入我们 */
.join { margin-top: 32px; padding: 24px 28px; display: flex; gap: 40px; align-items: center; justify-content: space-between; }
.joininfo { flex: 0 1 340px; min-width: 0; }
.joininfo h2 { margin: 0 0 8px; font-size: 22px; letter-spacing: 1px; }
.joininfo > p { margin: 0 0 14px; color: var(--ink-2); font-size: 13px; line-height: 1.7; }
.joinlist { list-style: none; margin: 0 0 18px; padding: 0; display: flex; flex-direction: column; gap: 8px; }
.joinlist li { display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--ink-2); }
.joinlist li span { font-size: 15px; }
.joinbtns { display: flex; gap: 10px; flex-wrap: wrap; }
.joingallery { display: flex; gap: 14px; flex-shrink: 0; }
.joinitem {
  width: 180px; border-radius: 10px; overflow: hidden;
  cursor: pointer; box-shadow: 0 8px 20px rgba(31, 35, 41, .12);
}
.joinitem img { width: 100%; height: 240px; object-fit: cover; display: block; transition: transform .35s; }
.joinitem:hover img { transform: scale(1.05); }

/* 平台服务 */
.service { margin-top: 32px; padding: 20px 24px; display: flex; gap: 34px; align-items: center; flex-wrap: wrap; }
.sitem { display: flex; gap: 10px; align-items: center; }
.sicon { font-size: 22px; }
.sitem b { font-size: 14px; }
.sitem p { margin: 2px 0 0; font-size: 12px; color: var(--ink-3); }
.ai-entry {
  margin-left: auto; display: flex; align-items: center; gap: 16px;
  padding: 10px 16px; border-radius: 10px; cursor: pointer;
  background: linear-gradient(100deg, #ffffff 0%, #e9fbfa 100%); border: 1px solid #d8f2f1;
}
.ai-entry b { font-size: 14px; }
.ai-entry p { margin: 2px 0 0; font-size: 12px; color: var(--ink-3); }

@media (max-width: 900px) {
  .join { flex-direction: column; align-items: stretch; gap: 18px; }
  .joininfo { flex: none; }
  .joingallery { justify-content: flex-start; overflow-x: auto; padding-bottom: 6px; }
  .joinitem { width: 150px; }
  .joinitem img { height: 200px; }
  .catnav { flex-direction: column; min-height: 0; }
  .catblock { flex: none; }
  .catfuncs { display: none; }
  .catpanel { max-height: none; padding: 14px 16px; }
  .gname { width: 84px; }
  .search { flex-wrap: wrap; }
  .citysel { width: 100%; }
  .ai-entry { margin-left: 0; }
}
</style>
