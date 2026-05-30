import { get, post, put, del } from '@/utils/request'
import type { Project, ProjectUser, ProjectCreateRequest, ProjectMemberRequest } from '@/types'

export interface ProjectVO extends Project {
  createUserName: string
  createUserAccount: string
  isOwner: boolean
  permission: 'r' | 'rw'
}

export function createProject(data: ProjectCreateRequest): Promise<Project> {
  return post('/project', data)
}

/** 获取团队项目列表（包含创建者信息） */
export function getTeamProjects(teamId: string): Promise<ProjectVO[]> {
  return get(`/project/team/${teamId}`)
}

export function getProjectById(projectId: string): Promise<Project> {
  return get(`/project/${projectId}`)
}

export function addProjectMember(projectId: string, data: ProjectMemberRequest): Promise<void> {
  return post(`/project/${projectId}/member`, data)
}

export function removeProjectMember(projectId: string, userId: string): Promise<void> {
  return del(`/project/${projectId}/member/${userId}`)
}

export interface ProjectMemberVO extends ProjectUser {
  userName: string
  account: string
  userIcon: string
}

/** 获取项目成员（包含用户信息） */
export function getProjectMembers(projectId: string): Promise<ProjectMemberVO[]> {
  return get(`/project/${projectId}/members`)
}

/** 更新项目基本信息 */
export function updateProject(projectId: string, data: Partial<Project>): Promise<Project> {
  return put(`/project/${projectId}`, data)
}

/** 删除项目 */
export function deleteProject(projectId: string): Promise<void> {
  return del(`/project/${projectId}`)
}

/** 更新项目成员权限 */
export function updateProjectMember(projectId: string, userId: string, data: { type?: string; permission?: string }): Promise<void> {
  return put(`/project/${projectId}/member/${userId}`, data)
}
