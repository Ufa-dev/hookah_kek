import { useState } from 'react'
import { Download } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { reportsApi } from '@/lib/api'

export default function ReportsPage() {
  const [isLoading, setIsLoading] = useState(false)

  const handleDownload = async () => {
    setIsLoading(true)
    try {
      const response = await reportsApi.downloadStock()
      const url = URL.createObjectURL(response.data)
      const a = document.createElement('a')
      a.href = url
      a.download = 'ostatok.xlsx'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
    } catch {
      toast.error('Не удалось выгрузить отчёт')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="page-root">
      <div className="page-container page-enter">
        <div className="flex items-center justify-between mb-6">
          <h1 className="font-display text-2xl text-ink">Отчёты</h1>
        </div>

        <div className="card p-6 flex flex-col gap-4 max-w-sm">
          <div>
            <h2 className="font-display text-lg text-ink mb-1">Остатки табака</h2>
            <p className="text-sm text-ink-dim">
              Сводный отчёт по остаткам: склад (market_arc) + контейнеры (flavor_pack), объединённые по вкусу.
            </p>
          </div>
          <Button
            variant="primary"
            onClick={handleDownload}
            disabled={isLoading}
            className="self-start"
          >
            <Download className="h-4 w-4" />
            {isLoading ? 'Формируется…' : 'Выгрузить остатки'}
          </Button>
        </div>
      </div>
    </div>
  )
}
