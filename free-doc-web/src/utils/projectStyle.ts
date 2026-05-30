export interface ProjectStyleConfig {
  value: string
  label: string
  bgClass: string
  iconClass: string
  cardClass: string
  headerClass: string
  iconBgClass: string
  iconColor: string
  nameColor: string
  descColor: string
}

export const projectStyles: ProjectStyleConfig[] = [
  {
    value: 'default',
    label: '经典蓝',
    bgClass: 'bg-gradient-to-br from-blue-500 to-blue-600',
    iconClass: 'text-white',
    cardClass: 'border-blue-200 bg-gradient-to-br from-blue-50 to-white',
    headerClass: 'bg-gradient-to-r from-blue-500 to-blue-600',
    iconBgClass: 'bg-gradient-to-br from-blue-500 to-blue-600',
    iconColor: 'text-white',
    nameColor: 'text-gray-900',
    descColor: 'text-gray-500',
  },
  {
    value: 'emerald',
    label: '翡翠绿',
    bgClass: 'bg-gradient-to-br from-emerald-500 to-emerald-600',
    iconClass: 'text-white',
    cardClass: 'border-emerald-200 bg-gradient-to-br from-emerald-50 to-white',
    headerClass: 'bg-gradient-to-r from-emerald-500 to-emerald-600',
    iconBgClass: 'bg-gradient-to-br from-emerald-500 to-emerald-600',
    iconColor: 'text-white',
    nameColor: 'text-gray-900',
    descColor: 'text-gray-500',
  },
  {
    value: 'violet',
    label: '梦幻紫',
    bgClass: 'bg-gradient-to-br from-violet-500 to-purple-600',
    iconClass: 'text-white',
    cardClass: 'border-violet-200 bg-gradient-to-br from-violet-50 to-white',
    headerClass: 'bg-gradient-to-r from-violet-500 to-purple-600',
    iconBgClass: 'bg-gradient-to-br from-violet-500 to-purple-600',
    iconColor: 'text-white',
    nameColor: 'text-gray-900',
    descColor: 'text-gray-500',
  },
  {
    value: 'amber',
    label: '活力橙',
    bgClass: 'bg-gradient-to-br from-amber-500 to-orange-500',
    iconClass: 'text-white',
    cardClass: 'border-amber-200 bg-gradient-to-br from-amber-50 to-white',
    headerClass: 'bg-gradient-to-r from-amber-500 to-orange-500',
    iconBgClass: 'bg-gradient-to-br from-amber-500 to-orange-500',
    iconColor: 'text-white',
    nameColor: 'text-gray-900',
    descColor: 'text-gray-500',
  },
  {
    value: 'rose',
    label: '浪漫粉',
    bgClass: 'bg-gradient-to-br from-rose-500 to-pink-500',
    iconClass: 'text-white',
    cardClass: 'border-rose-200 bg-gradient-to-br from-rose-50 to-white',
    headerClass: 'bg-gradient-to-r from-rose-500 to-pink-500',
    iconBgClass: 'bg-gradient-to-br from-rose-500 to-pink-500',
    iconColor: 'text-white',
    nameColor: 'text-gray-900',
    descColor: 'text-gray-500',
  },
]

export function getStyleConfig(styleValue: string): ProjectStyleConfig {
  return projectStyles.find(s => s.value === styleValue) || projectStyles[0]
}

export function parseIconWithStyle(iconField: string): { icon: string; style: string } {
  if (!iconField) return { icon: 'fa-solid fa-folder', style: 'default' }
  const parts = iconField.split('|')
  return {
    icon: parts[0] || 'fa-solid fa-folder',
    style: parts[1] || 'default',
  }
}

export function buildIconWithStyle(icon: string, style: string): string {
  if (!style || style === 'default') return icon
  return `${icon}|${style}`
}
