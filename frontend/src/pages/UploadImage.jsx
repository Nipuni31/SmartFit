/* eslint-disable react/prop-types */
import { useEffect, useState } from 'react'

export default function UploadImage({ frontFile, sideFile, onFileSelect }) {
  const [frontPreview, setFrontPreview] = useState(null)
  const [sidePreview, setSidePreview] = useState(null)

  useEffect(() => {
    if (!frontFile) {
      setFrontPreview(null)
      return
    }
    const url = URL.createObjectURL(frontFile)
    setFrontPreview(url)
    return () => URL.revokeObjectURL(url)
  }, [frontFile])

  useEffect(() => {
    if (!sideFile) {
      setSidePreview(null)
      return
    }
    const url = URL.createObjectURL(sideFile)
    setSidePreview(url)
    return () => URL.revokeObjectURL(url)
  }, [sideFile])

  return (
    <div className="upload-grid">
      <div className="upload-card">
        <label className="upload-label" htmlFor="front-image">Front Image</label>
        <input
          id="front-image"
          type="file"
          accept="image/*"
          onChange={e => onFileSelect('front', e.target.files?.[0] || null)}
        />
        {frontPreview && <img className="preview-image" src={frontPreview} alt="Front preview" />}
      </div>

      <div className="upload-card">
        <label className="upload-label" htmlFor="side-image">Side Image</label>
        <input
          id="side-image"
          type="file"
          accept="image/*"
          onChange={e => onFileSelect('side', e.target.files?.[0] || null)}
        />
        {sidePreview && <img className="preview-image" src={sidePreview} alt="Side preview" />}
      </div>
    </div>
  )
}
