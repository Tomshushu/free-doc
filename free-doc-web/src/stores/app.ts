import { defineStore } from 'pinia'
import { ref } from 'vue'

const TEAM_ID_KEY = 'freedoc-current-team-id'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const currentTeamId = ref<string | null>(localStorage.getItem(TEAM_ID_KEY))
  const currentProjectId = ref<string | null>(null)
  const currentProjectName = ref('')

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setCurrentTeam(teamId: string | null) {
    currentTeamId.value = teamId
    if (teamId) {
      localStorage.setItem(TEAM_ID_KEY, teamId)
    } else {
      localStorage.removeItem(TEAM_ID_KEY)
    }
  }

  function setCurrentProject(projectId: string | null) {
    currentProjectId.value = projectId
  }

  function setProjectName(name: string) {
    currentProjectName.value = name
  }

  return {
    sidebarCollapsed,
    currentTeamId,
    currentProjectId,
    currentProjectName,
    toggleSidebar,
    setCurrentTeam,
    setCurrentProject,
    setProjectName
  }
})
