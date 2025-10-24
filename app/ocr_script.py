import os
import torch
from PIL import Image
from tqdm import tqdm
import pdf2image
from transformers import DonutProcessor, VisionEncoderDecoderModel
from glob import glob

# Set up paths
input_folder = "input/val/images"
output_folder = "output"
os.makedirs(output_folder, exist_ok=True)

# For Windows, set your poppler path here if it's not in system PATH
poppler_path = r'path/to/poppler'

# Load model and processor
print("Loading OCR model...")
processor = DonutProcessor.from_pretrained("chinmays18/medical-prescription-ocr")
model = VisionEncoderDecoderModel.from_pretrained("chinmays18/medical-prescription-ocr")

# Set up device (GPU if available, else CPU)
device = "cuda" if torch.cuda.is_available() else "cpu"
print(f"Using device: {device}")
model.to(device)

# Get list of images
image_paths = glob(os.path.join(input_folder, "*.jpg")) + \
             glob(os.path.join(input_folder, "*.png")) + \
             glob(os.path.join(input_folder, "*.jpeg"))
pdf_paths = glob(os.path.join(input_folder, "*.pdf"))

print(f"Found {len(image_paths)} images and {len(pdf_paths)} PDFs to process")

def process_image(image, output_path):
    """Process a single image and save the OCR result"""
    pixel_values = processor(images=image, return_tensors="pt").pixel_values.to(device)
    task_prompt = "<s_ocr>"
    decoder_input_ids = processor.tokenizer(task_prompt, return_tensors="pt").input_ids.to(device)
    
    generated_ids = model.generate(
        pixel_values,
        decoder_input_ids=decoder_input_ids,
        max_length=512,
        num_beams=1,
        early_stopping=True
    )
    
    generated_text = processor.batch_decode(generated_ids, skip_special_tokens=True)[0]
    
    with open(output_path, "w", encoding="utf-8") as f:
        f.write(generated_text)
    
    return generated_text

# Process regular images
print("\nProcessing images...")
for img_path in tqdm(image_paths, desc="Processing images"):
    try:
        image = Image.open(img_path).convert("RGB")
        base_name = os.path.splitext(os.path.basename(img_path))[0]
        result_path = os.path.join(output_folder, f"{base_name}.txt")
        
        generated_text = process_image(image, result_path)
        print(f"Processed {img_path} -> {result_path}")
        print(f"Extracted text: {generated_text[:100]}...")  # Print first 100 chars
        
    except Exception as e:
        print(f"Error processing {img_path}: {str(e)}")

# Process PDFs
print("\nProcessing PDFs...")
for pdf_path in tqdm(pdf_paths, desc="Processing PDFs"):
    try:
        images = pdf2image.convert_from_path(pdf_path, poppler_path=poppler_path)
        all_text = []
        
        for i, image in enumerate(images):
            image = image.convert("RGB")
            base_name = os.path.splitext(os.path.basename(pdf_path))[0]
            result_path = os.path.join(output_folder, f"{base_name}_page{i+1}.txt")
            
            generated_text = process_image(image, result_path)
            all_text.append(f"--- Page {i+1} ---\n{generated_text}\n")
            print(f"Processed {pdf_path} (page {i+1}) -> {result_path}")
        
        # Save combined result for the PDF
        combined_path = os.path.join(output_folder, f"{base_name}_complete.txt")
        with open(combined_path, "w", encoding="utf-8") as f:
            f.writelines(all_text)
            
    except Exception as e:
        print(f"Error processing {pdf_path}: {str(e)}")

print("\nProcessing complete! Results are saved in the 'output' folder.")