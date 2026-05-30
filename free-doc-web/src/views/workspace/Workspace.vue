<template>
  <div class="workspace-page h-screen flex flex-col overflow-hidden bg-gradient-to-br from-slate-50 via-blue-50/30 to-indigo-50/40">
    <!-- 顶部导航栏 -->
    <header class="bg-white/80 backdrop-blur-xl border-b border-gray-200/80 shadow-sm z-50">
      <div class="px-1 sm:px-2 lg:px-3 py-4">
        <div class="flex items-center justify-between">
          <!-- 左侧：Logo + 团队选择 -->
          <div class="flex items-center gap-6">
            <div class="flex items-center gap-3 cursor-pointer" @click="router.push('/')">
              <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-600 to-indigo-600 flex items-center justify-center shadow-lg">
                <i class="fa-solid fa-file-lines text-white text-lg"></i>
              </div>
              <div>
                <h1 class="text-xl font-bold text-gray-900">FreeDoc</h1>
                <p class="text-xs text-gray-500">{{ $t('common.appSlogan') }}</p>
              </div>
            </div>
            
            <div class="h-8 w-px bg-gray-200"></div>
            
            <!-- 团队选择器 -->
            <el-select
              v-model="currentTeamId"
              :placeholder="$t('workspace.selectTeam')"
              class="!w-48"
              size="large"
              @change="handleSwitchTeam"
            >
              <template #prefix>
                <i class="fa-solid fa-users text-gray-400"></i>
              </template>
              <el-option
                v-for="team in allTeams"
                :key="team.teamId"
                :label="team.teamName"
                :value="team.teamId"
              >
                <div class="flex items-center gap-2">
                  <i :class="[team.teamIcon || 'fa-solid fa-users', 'text-blue-500']"></i>
                  <span>{{ team.teamName }}</span>
                </div>
              </el-option>
            </el-select>
          </div>

          <!-- 右侧：操作按钮 + 用户信息 -->
          <div class="flex items-center gap-3">
            <button 
              class="px-4 py-2 rounded-lg border border-gray-200 text-gray-700 font-medium hover:bg-gray-50 transition-colors flex items-center gap-2"
              @click="showCreateTeamDialog = true"
            >
              <i class="fa-solid fa-users-gear"></i>
              <span>{{ $t('workspace.newTeamBtn') }}</span>
            </button>

            <div class="h-8 w-px bg-gray-200 mx-2"></div>

            <LanguageToggle button-class="p-2.5" icon-size="text-lg" />
            <FontSizeToggle button-class="p-2.5" icon-size="text-lg" />
            <FontFamilyToggle button-class="p-2.5" icon-size="text-lg" />

            <!-- 用户下拉菜单 -->
            <el-dropdown trigger="click" @command="handleUserCommand">
              <div class="flex items-center gap-2 cursor-pointer hover:bg-gray-50 px-3 py-2 rounded-lg transition-colors">
                <div class="w-9 h-9 rounded-full bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center text-white text-sm font-bold shadow-sm">
                  {{ (userStore.user?.userName || '').charAt(0) || 'U' }}
                </div>
                <div class="text-left">
                  <p class="text-sm font-medium text-gray-900">{{ userStore.user?.userName }}</p>
                  <p class="text-xs text-gray-500">{{ userStore.user?.account }}</p>
                </div>
                <i class="fa-solid fa-chevron-down text-xs text-gray-400"></i>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="!rounded-xl !p-2 !min-w-[200px]">
                  <el-dropdown-item command="profile" class="!rounded-lg !px-3 !py-2.5">
                    <div class="flex items-center gap-3">
                      <i class="fa-solid fa-user text-gray-500"></i>
                      <span>{{ $t('common.profile') }}</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout" class="!rounded-lg !px-3 !py-2.5 !text-red-500 hover:!bg-red-50">
                    <div class="flex items-center gap-3">
                      <i class="fa-solid fa-right-from-bracket"></i>
                      <span>{{ $t('common.logout') }}</span>
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <!-- 更多操作 -->
            <el-dropdown trigger="click" @command="handleTopAction">
              <button class="p-2.5 rounded-lg hover:bg-gray-100 transition-colors text-gray-600">
                <i class="fa-solid fa-ellipsis-vertical text-lg"></i>
              </button>
              <template #dropdown>
                <el-dropdown-menu class="!rounded-xl !p-2">
                  <el-dropdown-item command="settings" class="!rounded-lg">
                    <div class="flex items-center gap-3">
                      <i class="fa-solid fa-gear text-gray-500"></i>
                      <span>{{ $t('common.systemSettings') }}</span>
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="flex-1 overflow-y-auto">
      <div class="max-w-[1500px] mx-auto px-1 sm:px-2 lg:px-3 py-8">
        
        <!-- 数据统计卡片区域 -->
        <div class="grid grid-cols-4 gap-4 mb-6">
          <!-- 项目数量卡片 -->
          <div class="bg-white rounded-xl p-4 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
            <div class="flex items-center justify-between mb-2">
              <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center">
                <i class="fa-solid fa-folder text-white text-lg"></i>
              </div>
              <span class="text-xs font-medium text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full">{{ $t('layout.project') }}</span>
            </div>
            <div class="space-y-0.5">
              <p class="text-2xl font-bold text-gray-900">{{ teamStats?.projectCount || 0 }}</p>
              <p class="text-xs text-gray-500">{{ $t('workspace.totalProjects') }}</p>
            </div>
          </div>

          <!-- 文档数量卡片 -->
          <div class="bg-white rounded-xl p-4 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
            <div class="flex items-center justify-between mb-2">
              <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-indigo-500 to-indigo-600 flex items-center justify-center">
                <i class="fa-solid fa-file-lines text-white text-lg"></i>
              </div>
              <span class="text-xs font-medium text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded-full">{{ $t('workspace.docCount') }}</span>
            </div>
            <div class="space-y-0.5">
              <p class="text-2xl font-bold text-gray-900">{{ teamStats?.docCount || 0 }}</p>
              <p class="text-xs text-gray-500">{{ $t('workspace.totalDocs') }}</p>
            </div>
          </div>

          <!-- 成员数量卡片 -->
          <div class="bg-white rounded-xl p-4 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
            <div class="flex items-center justify-between mb-2">
              <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-purple-500 to-purple-600 flex items-center justify-center">
                <i class="fa-solid fa-users text-white text-lg"></i>
              </div>
              <span class="text-xs font-medium text-purple-600 bg-purple-50 px-2 py-0.5 rounded-full">{{ $t('workspace.memberCount') }}</span>
            </div>
            <div class="space-y-0.5">
              <p class="text-2xl font-bold text-gray-900">{{ teamStats?.memberCount || 0 }}</p>
              <p class="text-xs text-gray-500">{{ $t('workspace.totalMembers') }}</p>
            </div>
          </div>

          <!-- 团队信息卡片 - 带成员下拉 -->
          <div class="bg-gradient-to-br from-blue-600 to-indigo-600 rounded-xl p-4 shadow-lg text-white relative overflow-hidden">
            <div class="absolute top-0 right-0 w-24 h-24 bg-white/10 rounded-full -mr-12 -mt-12"></div>
            <div class="absolute bottom-0 left-0 w-20 h-20 bg-white/10 rounded-full -ml-10 -mb-10"></div>
            <div class="relative z-10">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-1.5">
                  <i class="fa-solid fa-crown text-yellow-300 text-sm"></i>
                  <span class="text-xs font-medium text-white/90">{{ $t('workspace.currentTeam') }}</span>
                </div>
                <div class="flex items-center gap-1">
                  <!-- 团队成员下拉按钮 -->
                  <el-dropdown v-if="isTeamOwner" trigger="click" @command="handleMemberCommand">
                    <button
                      class="w-8 h-8 rounded-lg bg-white/10 hover:bg-white/20 flex items-center justify-center text-white/80 hover:text-white transition-all"
                      :title="$t('workspace.teamMembers')"
                    >
                      <i class="fa-solid fa-users text-sm"></i>
                    </button>
                    <template #dropdown>
                      <el-dropdown-menu class="!rounded-xl !p-2 !min-w-[160px]">
                        <el-dropdown-item command="createUser" class="!rounded-lg !px-3 !py-2">
                          <div class="flex items-center gap-2">
                            <i class="fa-solid fa-user-plus text-blue-500"></i>
                            <span>{{ $t('workspace.newUser') }}</span>
                          </div>
                        </el-dropdown-item>
                        <el-dropdown-item command="inviteMember" class="!rounded-lg !px-3 !py-2">
                          <div class="flex items-center gap-2">
                            <i class="fa-solid fa-user-plus text-purple-500"></i>
                            <span>{{ $t('workspace.inviteMember') }}</span>
                          </div>
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                  <!-- 删除团队按钮 -->
                  <button
                    v-if="isTeamOwner"
                    class="w-8 h-8 rounded-lg bg-white/10 hover:bg-red-500 flex items-center justify-center text-white/60 hover:text-white transition-all"
                    :title="$t('workspace.deleteTeam')"
                    @click="handleDeleteTeam"
                  >
                    <i class="fa-solid fa-trash-can text-sm"></i>
                  </button>
                </div>
              </div>
              <div class="space-y-0.5 mt-2">
                <p class="text-lg font-bold truncate">{{ teamStats?.teamName || $t('workspace.notSelected') }}</p>
                <p class="text-xs text-white/80 truncate">{{ teamStats?.createUserName || '-' }}</p>
              </div>
              <button 
                v-if="isTeamOwner"
                class="mt-3 px-2.5 py-1 bg-white/20 hover:bg-white/30 rounded-lg text-xs font-medium transition-colors flex items-center gap-1.5"
                @click="handleTeamSettingCommand('edit')"
              >
                <i class="fa-solid fa-gear text-xs"></i>
                <span>{{ $t('common.settings') }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- 项目列表区域 -->
        <div class="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
          <div class="flex items-center justify-between mb-6">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center shadow-md">
                <i class="fa-solid fa-folder text-white text-xl"></i>
              </div>
              <div>
                <h3 class="text-xl font-bold text-gray-900">{{ $t('workspace.teamProjects') }}</h3>
                <p class="text-sm text-gray-500">{{ $t('workspace.teamProjectsDesc') }}</p>
              </div>
            </div>
            <div class="flex items-center gap-3">
              <!-- 卡片/列表视图切换 -->
              <div class="flex items-center bg-gray-100 rounded-lg p-0.5">
                <button
                  class="px-3 py-1.5 rounded-md text-sm font-medium transition-all flex items-center gap-1.5"
                  :class="viewMode === 'card' ? 'bg-white text-primary shadow-sm' : 'text-gray-500 hover:text-gray-700'"
                  @click="viewMode = 'card'"
                >
                  <i class="fa-solid fa-grip text-xs"></i>
                  <span>{{ $t('workspace.cardView') }}</span>
                </button>
                <button
                  class="px-3 py-1.5 rounded-md text-sm font-medium transition-all flex items-center gap-1.5"
                  :class="viewMode === 'list' ? 'bg-white text-primary shadow-sm' : 'text-gray-500 hover:text-gray-700'"
                  @click="viewMode = 'list'"
                >
                  <i class="fa-solid fa-list text-xs"></i>
                  <span>{{ $t('workspace.listView') }}</span>
                </button>
              </div>

              <el-button v-if="isTeamOwner" type="primary" class="!bg-primary" @click="openCreateProjectDialog">
                <i class="fa-solid fa-plus mr-1"></i>
                <span>{{ $t('workspace.newProjectBtn') }}</span>
              </el-button>
              <span class="text-sm text-gray-500 bg-gray-100 px-4 py-2 rounded-lg font-medium">
                {{ $t('workspace.projectCount', { n: projects.length }) }}
              </span>
            </div>
          </div>

          <!-- 卡片视图 -->
          <div v-if="viewMode === 'card' && projects.length > 0" class="grid grid-cols-3 gap-4">
            <div
              v-for="proj in projects"
              :key="proj.projectId"
              class="project-card rounded-xl border-2 overflow-hidden cursor-pointer transition-all hover:shadow-lg hover:-translate-y-0.5 group"
              :class="getCardStyle(proj).cardClass"
              @click="selectProject(proj)"
            >
              <!-- 卡片头部 -->
              <div class="h-20 relative" :class="getCardStyle(proj).headerClass">
                <div class="absolute inset-0 flex items-center justify-center">
                  <div class="w-14 h-14 rounded-xl bg-white/20 backdrop-blur-sm flex items-center justify-center shadow-lg">
                    <i :class="[getParsedIcon(proj), 'text-2xl', getCardStyle(proj).iconColor]"></i>
                  </div>
                </div>
                <!-- 操作按钮 -->
                <div class="absolute top-2 right-2 flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity" @click.stop>
                  <button
                    v-if="proj.isOwner"
                    class="w-7 h-7 rounded-md bg-white/20 hover:bg-white/40 flex items-center justify-center text-white/80 hover:text-white transition-all"
                    @click.stop="handleProjectAction('members', proj)"
                    :title="$t('workspace.memberManagement')"
                  >
                    <i class="fa-solid fa-users text-xs"></i>
                  </button>
                  <button
                    v-if="proj.isOwner || proj.permission === 'rw'"
                    class="w-7 h-7 rounded-md bg-white/20 hover:bg-white/40 flex items-center justify-center text-white/80 hover:text-white transition-all"
                    @click.stop="handleProjectAction('edit', proj)"
                    :title="$t('common.edit')"
                  >
                    <i class="fa-solid fa-pen text-xs"></i>
                  </button>
                  <button
                    v-if="proj.isOwner"
                    class="w-7 h-7 rounded-md bg-white/20 hover:bg-red-500/80 flex items-center justify-center text-white/80 hover:text-white transition-all"
                    @click.stop="handleProjectAction('delete', proj)"
                    :title="$t('common.delete')"
                  >
                    <i class="fa-solid fa-trash-can text-xs"></i>
                  </button>
                </div>
                <!-- 拥有者标签 -->
                <div v-if="isProjectOwner(proj.projectId)" class="absolute top-2 left-2">
                  <span class="text-[10px] px-1.5 py-0.5 rounded-full bg-yellow-400/90 text-yellow-900 font-bold flex items-center gap-0.5">
                    <i class="fa-solid fa-crown text-[8px]"></i>{{ $t('common.owner') }}
                  </span>
                </div>
              </div>
              <!-- 卡片内容 -->
              <div class="p-4">
                <h4 class="font-bold text-base truncate" :class="getCardStyle(proj).nameColor">{{ proj.projectName }}</h4>
                <p class="text-sm mt-1 line-clamp-2" :class="getCardStyle(proj).descColor">{{ proj.projectDesc || $t('common.noDescription') }}</p>
                <div class="flex items-center justify-between mt-3 pt-3 border-t border-gray-100">
                  <div class="flex items-center gap-1.5">
                    <div class="w-6 h-6 rounded-full bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center text-white text-[10px] font-bold">
                      {{ (proj.createUserName || '').charAt(0) || 'U' }}
                    </div>
                    <span class="text-xs text-gray-500">{{ proj.createUserName || '-' }}</span>
                  </div>
                  <span class="text-xs text-gray-400">{{ formatDate(proj.createTime) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 列表视图 -->
          <div v-if="viewMode === 'list' && projects.length > 0">
            <el-table :data="projects" style="width: 100%" class="modern-table" @row-click="selectProject" :row-style="{ height: '72px' }">
              <el-table-column width="80">
                <template #default="{ row }">
                  <div class="w-12 h-12 rounded-xl flex items-center justify-center shadow-md" :class="getCardStyle(row).iconBgClass">
                    <i :class="[getParsedIcon(row), 'text-white text-lg']"></i>
                  </div>
                </template>
              </el-table-column>

              <el-table-column :label="$t('layout.projectName')" min-width="220">
                <template #default="{ row }">
                  <div class="flex items-center gap-2">
                    <span class="font-bold text-gray-900 text-base">{{ row.projectName }}</span>
                    <el-tag v-if="isProjectOwner(row.projectId)" size="small" effect="plain" round class="!text-yellow-700 !border-yellow-300 !bg-yellow-50">
                      <i class="fa-solid fa-crown mr-1 text-xs"></i>{{ $t('common.owner') }}
                    </el-tag>
                  </div>
                </template>
              </el-table-column>

              <el-table-column :label="$t('common.description')" min-width="280">
                <template #default="{ row }">
                  <span class="text-sm text-gray-600">{{ row.projectDesc || $t('common.noDescription') }}</span>
                </template>
              </el-table-column>

              <el-table-column :label="$t('common.creator')" width="180">
                <template #default="{ row }">
                  <div class="flex items-center gap-2.5">
                    <div class="w-9 h-9 rounded-full bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center text-white text-sm font-bold shadow-sm">
                      {{ (row.createUserName || '').charAt(0) || 'U' }}
                    </div>
                    <span class="text-sm text-gray-700 font-medium">{{ row.createUserName || row.createUserAccount || '-' }}</span>
                  </div>
                </template>
              </el-table-column>

              <el-table-column :label="$t('common.createTime')" width="140">
                <template #default="{ row }">
                  <span class="text-sm text-gray-500">{{ formatDate(row.createTime) }}</span>
                </template>
              </el-table-column>

              <el-table-column :label="$t('common.settings')" width="140" align="center">
                <template #default="{ row }">
                  <div class="flex items-center justify-center gap-1.5" @click.stop>
                    <el-tooltip v-if="row.isOwner" :content="$t('workspace.memberManagement')" placement="top">
                      <button 
                        class="w-9 h-9 rounded-lg hover:bg-blue-50 text-blue-600 transition-colors flex items-center justify-center"
                        @click.stop="handleProjectAction('members', row)"
                      >
                        <i class="fa-solid fa-users"></i>
                      </button>
                    </el-tooltip>
                    <el-tooltip v-if="row.isOwner || row.permission === 'rw'" :content="$t('common.edit')" placement="top">
                      <button 
                        class="w-9 h-9 rounded-lg hover:bg-gray-100 text-gray-600 transition-colors flex items-center justify-center"
                        @click.stop="handleProjectAction('edit', row)"
                      >
                        <i class="fa-solid fa-pen"></i>
                      </button>
                    </el-tooltip>
                    <el-tooltip v-if="row.isOwner" :content="$t('common.delete')" placement="top">
                      <button 
                        class="w-9 h-9 rounded-lg hover:bg-red-50 text-red-600 transition-colors flex items-center justify-center"
                        @click.stop="handleProjectAction('delete', row)"
                      >
                        <i class="fa-solid fa-trash-can"></i>
                      </button>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 空状态 -->
          <div v-if="projects.length === 0" class="py-16 text-center">
            <div class="inline-flex flex-col items-center">
              <div class="w-24 h-24 bg-gradient-to-br from-blue-50 to-indigo-50 rounded-2xl flex items-center justify-center mb-4">
                <i class="fa-solid fa-folder-open text-4xl text-blue-300"></i>
              </div>
              <p class="text-lg font-semibold text-gray-700 mb-2">{{ $t('workspace.noProjects') }}</p>
              <p class="text-sm text-gray-500 mb-6">{{ $t('workspace.noProjectsDesc') }}</p>
              <button 
                v-if="isTeamOwner"
                class="px-6 py-3 rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-medium hover:shadow-lg hover:scale-105 transition-all flex items-center gap-2"
                @click="openCreateProjectDialog"
              >
                <i class="fa-solid fa-plus"></i>
                <span>{{ $t('workspace.newProjectBtn') }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 创建团队弹窗 -->
    <el-dialog v-model="showCreateTeamDialog" :title="$t('layout.createTeam')" width="480px" destroy-on-close>
      <el-form :model="newTeamForm" label-width="80px">
        <el-form-item :label="$t('layout.teamName')" required>
          <el-input v-model="newTeamForm.teamName" :placeholder="$t('layout.teamNamePlaceholder')" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item :label="$t('layout.teamIcon')">
          <IconPicker v-model="newTeamForm.teamIcon" type="team" default-icon="fa-solid fa-users" />
        </el-form-item>
        <el-form-item :label="$t('layout.teamDesc')">
          <el-input v-model="newTeamForm.teamDesc" type="textarea" :rows="3" :placeholder="$t('layout.teamDescPlaceholder')" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateTeamDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!newTeamForm.teamName.trim()" @click="createTeam">{{ $t('common.create') }}</el-button>
      </template>
    </el-dialog>

    <!-- 编辑团队弹窗 -->
    <el-dialog v-model="showEditTeamDialog" :title="$t('workspace.editTeam')" width="480px" destroy-on-close>
      <el-form :model="editTeamForm" label-width="80px">
        <el-form-item :label="$t('layout.teamName')" required>
          <el-input v-model="editTeamForm.teamName" :placeholder="$t('layout.teamNamePlaceholder')" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item :label="$t('layout.teamIcon')">
          <IconPicker v-model="editTeamForm.teamIcon" type="team" default-icon="fa-solid fa-users" />
        </el-form-item>
        <el-form-item :label="$t('layout.teamDesc')">
          <el-input v-model="editTeamForm.teamDesc" type="textarea" :rows="3" :placeholder="$t('layout.teamDescPlaceholder')" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditTeamDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!editTeamForm.teamName.trim()" @click="saveEditTeam">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- 创建项目弹窗 -->
    <el-dialog v-model="showCreateProjectDialog" :title="$t('layout.createProject')" width="520px" destroy-on-close>
      <el-form :model="newProject" label-width="80px" @submit.prevent="createProject">
        <el-form-item :label="$t('layout.projectName')" required>
          <el-input v-model="newProject.projectName" :placeholder="$t('layout.projectNamePlaceholder')" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item :label="$t('layout.projectIcon')">
          <IconPicker v-model="newProject.projectIcon" type="project" default-icon="fa-solid fa-folder" />
        </el-form-item>
        <el-form-item :label="$t('layout.cardStyle')">
          <ProjectStylePicker v-model="newProject.projectStyle" :icon="newProject.projectIcon || 'fa-solid fa-folder'" />
        </el-form-item>
        <el-form-item :label="$t('layout.projectDesc')">
          <el-input v-model="newProject.projectDesc" type="textarea" :rows="3" :placeholder="$t('layout.projectDescPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateProjectDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!newProject.projectName.trim()" @click="createProject">{{ $t('common.create') }}</el-button>
      </template>
    </el-dialog>

    <!-- 编辑项目弹窗 -->
    <el-dialog v-model="showEditProjectDialog" :title="$t('workspace.editProject')" width="520px" destroy-on-close>
      <el-form :model="editProjectForm" label-width="80px">
        <el-form-item :label="$t('layout.projectName')" required>
          <el-input v-model="editProjectForm.name" :placeholder="$t('layout.projectNamePlaceholder')" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item :label="$t('layout.projectIcon')">
          <IconPicker v-model="editProjectForm.icon" type="project" default-icon="fa-solid fa-folder" />
        </el-form-item>
        <el-form-item :label="$t('layout.cardStyle')">
          <ProjectStylePicker v-model="editProjectForm.style" :icon="editProjectForm.icon || 'fa-solid fa-folder'" />
        </el-form-item>
        <el-form-item :label="$t('layout.projectDesc')">
          <el-input v-model="editProjectForm.desc" type="textarea" :rows="3" :placeholder="$t('layout.projectDescPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditProjectDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!editProjectForm.name.trim()" @click="saveEditProject">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- 项目成员弹窗（复用于团队成员管理） -->
    <el-dialog v-model="showMemberDialog" :title="activeProject ? `${$t('workspace.memberManagement')} - ${activeProject.projectName}` : $t('workspace.teamMembers')" width="640px" destroy-on-close>
      <div class="flex items-center justify-between mb-4">
        <span class="text-sm text-gray-500">{{ $t('workspace.memberList', { n: activeProjectMembers.length }) }}</span>
        <button v-if="canManageCurrentMembers" class="text-xs text-primary hover:text-primary/80 px-3 py-1.5 rounded-lg bg-primary/5 hover:bg-primary/10 transition-colors flex items-center gap-1 font-medium"
                @click="openAddMember">
          <i class="fa-solid fa-plus text-[10px]"></i>{{ $t('workspace.addMember') }}
        </button>
      </div>

      <el-table :data="activeProjectMembers" size="default" v-loading="memberDialogLoading">
        <el-table-column label="#" width="50">
          <template #default="{ $index }">
            <div class="w-6 h-6 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center text-xs font-bold">{{ $index + 1 }}</div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('workspace.username')" min-width="140">
          <template #default="{ row }">
            <span>{{ row.userName || row.name || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="userId" :label="$t('workspace.userId')" min-width="160">
          <template #default="{ row }">
            <span class="text-gray-400 text-xs font-mono">{{ row.userId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" :label="$t('common.role')" width="110">
          <template #default="{ row }">
            <el-tag
              :type="row.type === 'OWNER' ? 'warning' : 'success'"
              effect="plain"
              round
              size="small"
            >
              {{ row.type === 'OWNER' ? $t('common.owner') : $t('common.participant') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.permission')" width="100">
          <template #default="{ row }">
            <el-select :model-value="row.permission || (row.type === 'OWNER' ? 'rw' : 'r')" size="small" disabled class="!w-full">
              <el-option :label="$t('common.readOnly')" value="r" />
              <el-option :label="$t('common.readWrite')" value="rw" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column v-if="canManageCurrentMembers" :label="$t('common.settings')" width="70" align="center">
          <template #default="{ row }">
            <el-button text type="danger" size="small" :disabled="row.type === 'OWNER'" @click="handleRemoveMember(row)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="showAddMemberForm" class="mt-5 pt-4 border-t border-gray-100">
        <p class="text-sm font-medium text-gray-700 mb-3">{{ $t('workspace.addNewMember') }}</p>
        <div class="mb-3">
          <el-select
            v-model="addMemberForm.userId"
            filterable
            remote
            reserve-keyword
            :remote-method="searchSystemUsers"
            :placeholder="$t('workspace.searchUserPlaceholder')"
            class="!w-full"
            size="default"
          >
            <el-option
              v-for="u in searchUserResults"
              :key="u.userId"
              :label="`${u.userName}${u.account ? '（' + u.account + '）' : ''}`"
              :value="u.userId"
            >
              <div class="flex items-center gap-2">
                <span class="text-gray-700">{{ u.userName }}</span>
                <span class="text-xs text-gray-400">{{ u.account }}</span>
              </div>
            </el-option>
          </el-select>
        </div>
        <div class="flex items-end gap-3 bg-gray-50 rounded-lg p-3" v-if="selectedAddUser">
          <div class="flex-1">
            <p class="text-sm font-medium text-gray-800">{{ $t('workspace.selected') }}{{ selectedAddUser.userName }} ({{ selectedAddUser.account || '' }})</p>
          </div>
          <el-form-item :label="$t('common.role')" class="!mb-0 !mr-0">
            <el-select v-model="addMemberForm.type" style="width: 130px">
              <el-option :label="$t('workspace.admin')" value="OWNER" />
              <el-option :label="$t('workspace.participantRole')" value="PARTICIPANT" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('common.permission')" class="!mb-0 !mr-0">
            <el-select v-model="addMemberForm.permission" style="width: 90px">
              <el-option :label="$t('common.readWrite')" value="rw" />
              <el-option :label="$t('common.readOnly')" value="r" />
            </el-select>
          </el-form-item>
        </div>
        <div class="mt-3 flex justify-end gap-2">
          <el-button type="primary" :disabled="!addMemberForm.userId" @click="submitAddMember">{{ $t('workspace.confirmAdd') }}</el-button>
          <el-button @click="cancelAddMember">{{ $t('common.cancel') }}</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 新建用户弹窗 -->
    <el-dialog v-model="showCreateUserDialog" :title="$t('workspace.newUser')" width="520px" destroy-on-close>
      <el-form :model="userForm" label-width="80px">
        <el-form-item :label="$t('workspace.account')" required>
          <el-input v-model="userForm.account" :placeholder="$t('workspace.loginAccountPlaceholder')" maxlength="30" />
        </el-form-item>
        <el-form-item :label="$t('workspace.password')" required>
          <el-input v-model="userForm.password" type="password" :placeholder="$t('login.passwordPlaceholder')" show-password maxlength="32" />
        </el-form-item>
        <el-form-item :label="$t('workspace.username')">
          <el-input v-model="userForm.userName" :placeholder="$t('workspace.displayNamePlaceholder')" maxlength="20" />
        </el-form-item>
        <el-form-item :label="$t('workspace.userIcon')">
          <IconPicker v-model="userForm.userIcon" type="user" default-icon="fa-solid fa-user" />
        </el-form-item>
        <el-form-item :label="$t('common.role')">
          <el-radio-group v-model="userForm.role">
            <el-radio value="PARTICIPANT">{{ $t('workspace.normalMember') }}</el-radio>
            <el-radio value="OWNER">{{ $t('workspace.admin') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateUserDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!userForm.account.trim() || !userForm.password.trim()" @click="submitCreateUser">{{ $t('common.create') }}</el-button>
      </template>
    </el-dialog>

    <!-- 页脚 -->
    <footer class="shrink-0 text-center py-1.5 text-[11px] text-gray-400 border-t border-gray-100">
      © 2025-{{ currentYear }} tomshushu. Licensed under the MIT License.
      <a href="https://github.com/Tomshushu/free-doc" target="_blank" rel="noopener noreferrer" class="inline-flex items-center gap-1 ml-2 text-gray-400 hover:text-gray-600 transition-colors">
        <svg class="w-4 h-4" viewBox="0 0 16 16" fill="currentColor"><path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"/></svg>
        GitHub
      </a>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { getUserTeams, getTeamMembers, getTeamStats, createTeam as createTeamApi, updateTeam as updateTeamApi, deleteTeam as deleteTeamApi, addTeamMember, type TeamMemberVO, type TeamStatsVO } from '@/api/team'
import { getTeamProjects, createProject as createProjectApi, deleteProject as deleteProjectApi, updateProject as updateProjectApi, addProjectMember, removeProjectMember, getProjectMembers, type ProjectVO, type ProjectMemberVO } from '@/api/project'
import { removeTeamMember } from '@/api/team'
import { searchUsers } from '@/api/user'
import { register as registerApi } from '@/api/auth'
import LanguageToggle from '@/components/LanguageToggle.vue'
import FontSizeToggle from '@/components/FontSizeToggle.vue'
import FontFamilyToggle from '@/components/FontFamilyToggle.vue'
import IconPicker from '@/components/IconPicker.vue'
import ProjectStylePicker from '@/components/ProjectStylePicker.vue'
import { parseIconWithStyle, buildIconWithStyle, getStyleConfig } from '@/utils/projectStyle'
import type { Team } from '@/types'

const router = useRouter()
const { t } = useI18n()
const userStore = useUserStore()
const currentYear = new Date().getFullYear()
const appStore = useAppStore()

const allTeams = ref<Team[]>([])
const currentTeamId = ref<string>('')
const projects = ref<ProjectVO[]>([])
const teamMembers = ref<TeamMemberVO[]>([])
const teamStats = ref<TeamStatsVO | null>(null)
const viewMode = ref<'card' | 'list'>('card')

const showCreateTeamDialog = ref(false)
const showEditTeamDialog = ref(false)
const showCreateProjectDialog = ref(false)
const showMemberDialog = ref(false)
const showEditProjectDialog = ref(false)
const newTeamForm = ref({ teamName: '', teamIcon: 'fa-solid fa-users', teamDesc: '' })
const editTeamForm = ref({ teamId: '', teamName: '', teamIcon: 'fa-solid fa-users', teamDesc: '' })
const newProject = ref({ projectName: '', projectIcon: 'fa-solid fa-folder', projectStyle: 'default', projectDesc: '' })
const editProjectForm = ref<{ id: string; name: string; icon: string; style: string; desc: string }>({ id: '', name: '', icon: 'fa-solid fa-folder', style: 'default', desc: '' })

const activeProject = ref<ProjectVO | null>(null)
const activeProjectMembers = ref<ProjectMemberVO[]>([])
const memberDialogLoading = ref(false)

const showAddMemberForm = ref(false)
const addMemberForm = ref({ userId: '', type: 'PARTICIPANT', permission: 'r' })
const searchUserResults = ref<any[]>([])
const selectedAddUser = ref<any>(null)

const showCreateUserDialog = ref(false)
const userForm = ref({ account: '', password: '', userName: '', userIcon: 'fa-solid fa-user', role: 'PARTICIPANT' })

const currentTeamUserRole = computed(() => {
  const myMember = teamMembers.value.find(m => m.userId === userStore.user?.userId)
  if (!myMember) return ''
  return myMember.type === 'OWNER' ? t('common.creator') : t('common.participant')
})

const isTeamOwner = computed(() => {
  return teamStats.value?.currentUserType === 'OWNER'
})

const canManageCurrentMembers = computed(() => {
  if (activeProject.value) {
    return activeProject.value.isOwner
  }
  return isTeamOwner.value
})

function isProjectOwner(projectId: string): boolean {
  const project = projects.value.find(p => p.projectId === projectId)
  return project?.isOwner ?? false
}

function getParsedIcon(proj: ProjectVO): string {
  return parseIconWithStyle(proj.projectIcon).icon
}

function getCardStyle(proj: ProjectVO) {
  const style = parseIconWithStyle(proj.projectIcon).style
  return getStyleConfig(style)
}

function handleTeamSettingCommand(cmd: string) {
  if (cmd === 'edit') {
    openEditTeamDialog()
  } else if (cmd === 'delete') {
    handleDeleteTeam()
  }
}

function handleTopAction(cmd: string) {
  if (cmd === 'settings') {
    router.push('/settings')
  }
}

function handleUserCommand(cmd: string) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    ElMessageBox.confirm(t('layout.confirmLogout'), t('common.confirm'), {
      type: 'warning',
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel')
    }).then(() => {
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
  }
}

function handleMemberCommand(cmd: string) {
  if (cmd === 'createUser') {
    showCreateUserDialog.value = true
    resetUserForm()
  } else if (cmd === 'inviteMember') {
    showMemberDialog.value = true
    activeProject.value = null
    loadTeamMembersList()
  }
}

function openEditTeamDialog() {
  const currentTeam = allTeams.value.find(t => t.teamId === currentTeamId.value)
  if (currentTeam) {
    editTeamForm.value = {
      teamId: currentTeam.teamId,
      teamName: currentTeam.teamName,
      teamIcon: currentTeam.teamIcon || 'fa-solid fa-users',
      teamDesc: currentTeam.teamDesc || ''
    }
    showEditTeamDialog.value = true
  }
}

async function saveEditTeam() {
  if (!editTeamForm.value.teamName.trim()) return
  try {
    await updateTeamApi(editTeamForm.value.teamId, {
      teamName: editTeamForm.value.teamName.trim(),
      teamIcon: editTeamForm.value.teamIcon,
      teamDesc: editTeamForm.value.teamDesc
    })
    ElMessage.success(t('workspace.teamModified'))
    showEditTeamDialog.value = false
    await loadTeams()
  } catch (e: any) {
    ElMessage.error(e.message || t('common.modifyFailed'))
  }
}

async function handleDeleteTeam() {
  const teamName = teamStats.value?.teamName
  if (!teamName) return
  try {
    await ElMessageBox.confirm(
      t('workspace.confirmDeleteTeamAll', { name: teamName }),
      t('common.dangerAction'),
      {
        type: 'warning',
        confirmButtonText: t('common.confirmDelete'),
        cancelButtonText: t('common.cancel'),
        confirmButtonClass: 'el-button--danger'
      }
    )
    const teamId = currentTeamId.value || teamStats.value?.teamId
    if (teamId) {
      await deleteTeamApi(teamId)
      ElMessage.success(t('team.teamDeleted'))
      allTeams.value = allTeams.value.filter(t => t.teamId !== teamId)
      if (allTeams.value.length > 0) {
        currentTeamId.value = allTeams.value[0].teamId
        appStore.setCurrentTeam(allTeams.value[0].teamId)
        await loadTeamData(currentTeamId.value)
      } else {
        currentTeamId.value = ''
        appStore.setCurrentTeam(null)
        projects.value = []
        teamMembers.value = []
        teamStats.value = null
      }
    }
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || t('common.deleteFailed'))
    }
  }
}

function openCreateProjectDialog() {
  newProject.value = { projectName: '', projectIcon: 'fa-solid fa-folder', projectStyle: 'default', projectDesc: '' }
  showCreateProjectDialog.value = true
}

onMounted(async () => {
  await Promise.all([loadTeams()])
})

async function loadTeams() {
  try {
    const teams = await getUserTeams()
    allTeams.value = teams
    if (teams.length > 0) {
      if (appStore.currentTeamId && teams.some(t => t.teamId === appStore.currentTeamId)) {
        currentTeamId.value = appStore.currentTeamId!
      } else {
        currentTeamId.value = teams[0].teamId
        appStore.setCurrentTeam(teams[0].teamId)
      }
      await loadTeamData(currentTeamId.value)
    }
  } catch (e) {
    console.error(e)
  }
}

async function handleSwitchTeam(teamId: string) {
  if (!teamId) return
  currentTeamId.value = teamId
  appStore.setCurrentTeam(teamId)
  await loadTeamData(teamId)
}

async function loadTeamData(teamId: string) {
  try {
    const [projectList, members, stats] = await Promise.all([
      getTeamProjects(teamId),
      getTeamMembers(teamId),
      getTeamStats(teamId)
    ])
    projects.value = projectList
    teamMembers.value = members
    teamStats.value = stats
  } catch (e) {
    console.error('loadTeamData failed:', e)
  }
}

function selectProject(project: ProjectVO) {
  appStore.setCurrentProject(project.projectId)
  appStore.setProjectName(project.projectName)
  router.push(`/workspace/${project.projectId}`)
}

function handleProjectAction(cmd: string, proj: ProjectVO) {
  if (cmd === 'members') {
    activeProject.value = proj
    showMemberDialog.value = true
    loadProjectMemberList(proj.projectId)
  } else if (cmd === 'edit') {
    activeProject.value = proj
    const parsed = parseIconWithStyle(proj.projectIcon)
    editProjectForm.value = {
      id: proj.projectId,
      name: proj.projectName,
      icon: parsed.icon,
      style: parsed.style,
      desc: proj.projectDesc || ''
    }
    showEditProjectDialog.value = true
  } else if (cmd === 'delete') {
    ElMessageBox.confirm(
      t('layout.confirmDeleteProject', { name: proj.projectName }),
      t('common.dangerAction'),
      { type: 'warning', confirmButtonText: t('common.confirmDelete'), cancelButtonText: t('common.cancel') }
    ).then(async () => {
      try {
        await deleteProjectApi(proj.projectId)
        ElMessage.success(t('layout.projectDeleted'))
        projects.value = projects.value.filter(p => p.projectId !== proj.projectId)
        if (appStore.currentProjectId === proj.projectId) {
          appStore.setCurrentProject(null)
          router.push('/')
        }
      } catch (e: any) {
        ElMessage.error(e.message || t('common.deleteFailed'))
      }
    }).catch(() => {})
  }
}

async function loadTeamMembersList() {
  if (!currentTeamId.value) return
  memberDialogLoading.value = true
  try {
    const members = await getTeamMembers(currentTeamId.value)
    activeProjectMembers.value = members.map(m => ({
      ...m,
      projectId: currentTeamId.value,
      permission: (m.type === 'OWNER' ? 'rw' : 'r') as 'r' | 'rw'
    }))
  } catch (e) {
    console.error('loadTeamMembersList failed:', e)
    activeProjectMembers.value = []
  } finally {
    memberDialogLoading.value = false
  }
}

async function loadProjectMemberList(projectId: string) {
  memberDialogLoading.value = true
  try {
    activeProjectMembers.value = await getProjectMembers(projectId)
  } catch (e) {
    console.error('loadProjectMemberList failed:', e)
    activeProjectMembers.value = []
  } finally {
    memberDialogLoading.value = false
  }
}

async function searchSystemUsers(query: string) {
  if (!query) { searchUserResults.value = []; return }
  try {
    const users = await searchUsers(query)
    searchUserResults.value = users
  } catch {
    searchUserResults.value = []
  }
}

watch(() => addMemberForm.value.userId, (userId) => {
  if (!userId) {
    selectedAddUser.value = null
    return
  }
  const found = searchUserResults.value.find((u: any) => u.userId === userId)
  if (found) {
    selectedAddUser.value = found
  } else {
    selectedAddUser.value = null
  }
})

function openAddMember() {
  addMemberForm.value = { userId: '', type: 'PARTICIPANT', permission: 'r' }
  selectedAddUser.value = null
  searchUserResults.value = []
  showAddMemberForm.value = true
}

function cancelAddMember() {
  showAddMemberForm.value = false
  selectedAddUser.value = null
  searchUserResults.value = []
}

function resetUserForm() {
  userForm.value = { account: '', password: '', userName: '', userIcon: 'fa-solid fa-user', role: 'PARTICIPANT' }
}

async function submitCreateUser() {
  if (!userForm.value.account.trim() || !userForm.value.password.trim()) return
  try {
    const userId = await registerApi({
      account: userForm.value.account.trim(),
      password: userForm.value.password,
      userName: userForm.value.userName.trim() || undefined,
      userIcon: userForm.value.userIcon || undefined
    })
    ElMessage.success(t('workspace.userCreated'))
    showCreateUserDialog.value = false
    if (currentTeamId.value && userId) {
      try {
        await addTeamMember(currentTeamId.value, {
          userId: userId,
          type: userForm.value.role as 'OWNER' | 'PARTICIPANT'
        })
        ElMessage.success(t('workspace.autoJoinTeam', { role: userForm.value.role === 'OWNER' ? t('workspace.admin') : t('workspace.normalMember') }))
        await loadTeamMembersList()
      } catch (e: any) {
        console.warn('autoJoinTeam failed:', e)
      }
    }
    resetUserForm()
  } catch (e: any) {
    ElMessage.error(e.message || t('common.createFailed'))
  }
}

async function submitAddMember() {
  if (!addMemberForm.value.userId.trim()) {
    ElMessage.warning(t('workspace.pleaseEnterUserId'))
    return
  }
  try {
    if (activeProject.value) {
      await addProjectMember(activeProject.value.projectId, {
        userId: addMemberForm.value.userId,
        type: addMemberForm.value.type as 'OWNER' | 'PARTICIPANT',
        permission: addMemberForm.value.permission as 'r' | 'rw'
      })
      await loadProjectMemberList(activeProject.value.projectId)
    } else {
      await addTeamMember(currentTeamId.value, {
        userId: addMemberForm.value.userId,
        type: addMemberForm.value.type as 'OWNER' | 'PARTICIPANT'
      })
      await loadTeamMembersList()
    }
    ElMessage.success(t('workspace.memberAdded'))
    showAddMemberForm.value = false
  } catch (e: any) {
    ElMessage.error(e.message || t('workspace.addFailed'))
  }
}

async function handleRemoveMember(row: any) {
  if (activeProject.value) {
    // 项目成员删除
    try {
      await removeProjectMember(activeProject.value.projectId, row.userId)
      ElMessage.success(t('common.remove'))
      await loadProjectMemberList(activeProject.value.projectId)
    } catch (e: any) {
      ElMessage.error(e.message || t('common.deleteFailed'))
    }
  } else {
    // 团队成员删除
    try {
      await removeTeamMember(currentTeamId.value, row.userId)
      ElMessage.success(t('common.remove'))
      await loadTeamMembersList()
    } catch (e: any) {
      ElMessage.error(e.message || t('common.deleteFailed'))
    }
  }
}

async function saveEditProject() {
  if (!editProjectForm.value.name.trim()) return
  try {
    const iconWithStyle = buildIconWithStyle(editProjectForm.value.icon, editProjectForm.value.style)
    await updateProjectApi(editProjectForm.value.id, {
      projectName: editProjectForm.value.name,
      projectIcon: iconWithStyle,
      projectDesc: editProjectForm.value.desc
    })
    ElMessage.success(t('workspace.teamModified'))
    showEditProjectDialog.value = false
    const idx = projects.value.findIndex(p => p.projectId === editProjectForm.value.id)
    if (idx > -1) {
      projects.value[idx].projectName = editProjectForm.value.name
      projects.value[idx].projectIcon = iconWithStyle
      projects.value[idx].projectDesc = editProjectForm.value.desc
    }
  } catch (e: any) {
    ElMessage.error(e.message || t('common.modifyFailed'))
  }
}

async function createTeam() {
  if (!newTeamForm.value.teamName.trim()) return
  try {
    const team = await createTeamApi({
      teamName: newTeamForm.value.teamName.trim(),
      teamIcon: newTeamForm.value.teamIcon,
      teamDesc: newTeamForm.value.teamDesc
    })
    ElMessage.success(t('layout.teamCreated'))
    showCreateTeamDialog.value = false
    newTeamForm.value = { teamName: '', teamIcon: 'fa-solid fa-users', teamDesc: '' }
    allTeams.value.push(team)
    currentTeamId.value = team.teamId
    appStore.setCurrentTeam(team.teamId)
    await loadTeamData(team.teamId)
  } catch (e: any) {
    ElMessage.error(e.message || t('common.createFailed'))
  }
}

async function createProject() {
  if (!newProject.value.projectName.trim()) return
  try {
    const iconWithStyle = buildIconWithStyle(newProject.value.projectIcon, newProject.value.projectStyle)
    const p = await createProjectApi({
      projectName: newProject.value.projectName.trim(),
      projectIcon: iconWithStyle,
      projectDesc: newProject.value.projectDesc,
      teamId: currentTeamId.value
    })
    ElMessage.success(t('common.createSuccess'))
    showCreateProjectDialog.value = false
    newProject.value = { projectName: '', projectIcon: 'fa-solid fa-folder', projectStyle: 'default', projectDesc: '' }
    await loadTeamData(currentTeamId.value)
    appStore.setCurrentProject(p.projectId)
    appStore.setProjectName(p.projectName)
    router.push(`/workspace/${p.projectId}`)
  } catch (e: any) {
    ElMessage.error(e.message || t('common.createFailed'))
  }
}

function formatDate(date: string | undefined): string {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\//g, '/')
}
</script>

<style scoped>
:deep(.modern-table) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.modern-table .el-table__header-wrapper) {
  background: linear-gradient(to bottom, #f8fafc, #f1f5f9);
}

:deep(.modern-table .el-table__header th) {
  background: transparent;
  color: #64748b;
  font-weight: 600;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid #e2e8f0;
}

:deep(.modern-table .el-table__row) {
  cursor: pointer;
  transition: all 0.2s;
}

:deep(.modern-table .el-table__row:hover) {
  background-color: #f8fafc !important;
  transform: scale(1.002);
}

:deep(.modern-table .el-table__body td) {
  border-bottom: 1px solid #f1f5f9;
  padding: 20px 0;
  font-size: 14px;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.workspace-page > * {
  animation: fadeIn 0.3s ease-out;
}
</style>
