import { get, post, put, del } from '@/utils/request'
import type { Team, TeamUser, TeamCreateRequest, TeamMemberRequest } from '@/types'

export interface TeamMemberVO extends TeamUser {
  userName: string
  account: string
  userIcon: string
}

export interface TeamStatsVO {
  teamId: string
  teamName: string
  teamDesc: string
  createUser: string
  createUserName: string
  createTime: string
  projectCount: number
  memberCount: number
  docCount: number
  currentUserType: 'OWNER' | 'PARTICIPANT'
  currentUserPermission: string
}

export function createTeam(data: TeamCreateRequest): Promise<Team> {
  return post('/team', data)
}

export function getUserTeams(): Promise<Team[]> {
  return get('/team/list')
}

export function getTeamById(teamId: string): Promise<Team> {
  return get(`/team/${teamId}`)
}

export function addTeamMember(teamId: string, data: TeamMemberRequest): Promise<void> {
  return post(`/team/${teamId}/member`, data)
}

export function removeTeamMember(teamId: string, userId: string): Promise<void> {
  return del(`/team/${teamId}/member/${userId}`)
}

/** 获取团队成员（包含用户信息） */
export function getTeamMembers(teamId: string): Promise<TeamMemberVO[]> {
  return get(`/team/${teamId}/members`)
}

/** 获取团队统计信息 */
export function getTeamStats(teamId: string): Promise<TeamStatsVO> {
  return get(`/team/${teamId}/stats`)
}

/** 更新团队基本信息 */
export function updateTeam(teamId: string, data: Partial<Team>): Promise<Team> {
  return put(`/team/${teamId}`, data)
}

/** 删除团队 */
export function deleteTeam(teamId: string): Promise<void> {
  return del(`/team/${teamId}`)
}
