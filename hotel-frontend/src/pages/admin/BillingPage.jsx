import React, { useEffect, useState, useCallback } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { FileText, Download } from 'lucide-react'
import { fetchInvoices, createInvoice, selectInvoices, selectBillingLoading } from '../../store/slices/billingSlice'
import DataTable from '../../components/common/DataTable'
import Badge from '../../components/common/Badge'
import Modal from '../../components/common/Modal'

export default function BillingPage() {
  const dispatch = useDispatch()
  const invoices = useSelector(selectInvoices)
  const loading  = useSelector(selectBillingLoading)
  const [detail, setDetail] = useState(null)
  const [page, setPage]     = useState(0)

  useEffect(() => {
    dispatch(fetchInvoices({ page, size: 20 }))
  }, [dispatch, page])

  const downloadPdf = (invoice) => {
    // Client-side PDF generation with jsPDF
    import('jspdf').then(({ default: jsPDF }) => {
      const doc = new jsPDF()
      doc.setFontSize(18)
      doc.text('INVOICE', 20, 20)
      doc.setFontSize(12)
      doc.text(`Invoice #: ${invoice.invoiceNumber}`, 20, 35)
      doc.text(`Date: ${invoice.issuedAt?.split('T')[0] ?? '—'}`, 20, 45)
      doc.text(`Guest: ${invoice.guest?.firstName} ${invoice.guest?.lastName}`, 20, 55)
      doc.text(`Total: $${Number(invoice.totalAmount).toLocaleString()}`, 20, 65)
      doc.save(`invoice-${invoice.invoiceNumber}.pdf`)
    })
  }

  const columns = [
    { key: 'invoiceNumber', header: 'Invoice #', render: (v) => <span className="font-mono text-xs">{v}</span> },
    { key: 'guest',        header: 'Guest',   render: (_, r) => `${r.guest?.firstName ?? ''} ${r.guest?.lastName ?? ''}` },
    { key: 'totalAmount',  header: 'Total',   render: (v) => v ? `$${Number(v).toLocaleString()}` : '—' },
    { key: 'taxAmount',    header: 'Tax',     render: (v) => v ? `$${Number(v).toFixed(2)}` : '—' },
    { key: 'status',       header: 'Status',  render: (v) => <Badge status={v} /> },
    { key: 'issuedAt',     header: 'Issued',  render: (v) => v?.split('T')[0] ?? '—' },
    {
      key: 'id', header: 'PDF',
      render: (_, r) => (
        <button onClick={(e) => { e.stopPropagation(); downloadPdf(r) }}
          className="text-gray-400 hover:text-primary-600 transition-colors p-1">
          <Download size={15} />
        </button>
      )
    },
  ]

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <p className="text-sm text-gray-500">{invoices?.totalElements ?? 0} invoices</p>
      </div>

      <DataTable
        columns={columns}
        data={invoices?.content ?? []}
        loading={loading}
        pagination={{ page, size: 20, totalElements: invoices?.totalElements ?? 0, totalPages: invoices?.totalPages ?? 0 }}
        onPageChange={setPage}
        onRowClick={setDetail}
        emptyMessage="No invoices found"
      />

      <Modal isOpen={!!detail} onClose={() => setDetail(null)} title="Invoice Details" size="lg"
        footer={<>
          <button className="btn-secondary" onClick={() => setDetail(null)}>Close</button>
          <button className="btn-primary" onClick={() => downloadPdf(detail)}>
            <Download size={15} /> Download PDF
          </button>
        </>}>
        {detail && (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-3 text-sm">
              <div><p className="text-gray-500">Invoice #</p><p className="font-mono font-medium">{detail.invoiceNumber}</p></div>
              <div><p className="text-gray-500">Status</p><Badge status={detail.status} /></div>
              <div><p className="text-gray-500">Subtotal</p><p>${Number(detail.subtotal).toFixed(2)}</p></div>
              <div><p className="text-gray-500">Tax ({detail.taxRate}%)</p><p>${Number(detail.taxAmount).toFixed(2)}</p></div>
              <div><p className="text-gray-500">Total</p><p className="font-bold text-lg">${Number(detail.totalAmount).toFixed(2)}</p></div>
            </div>

            {detail.items?.length > 0 && (
              <div>
                <p className="text-sm font-semibold text-gray-700 mb-2">Line Items</p>
                <table className="w-full text-sm">
                  <thead><tr className="border-b"><th className="table-header">Description</th><th className="table-header">Qty</th><th className="table-header">Unit</th><th className="table-header">Total</th></tr></thead>
                  <tbody>
                    {detail.items.map(item => (
                      <tr key={item.id} className="border-b border-gray-50">
                        <td className="table-cell">{item.description}</td>
                        <td className="table-cell">{item.quantity}</td>
                        <td className="table-cell">${Number(item.unitPrice).toFixed(2)}</td>
                        <td className="table-cell font-medium">${Number(item.totalPrice).toFixed(2)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  )
}
