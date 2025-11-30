import re
from pathlib import Path

# Archivos a procesar
files_to_fix = [
    "src/main/java/com/harmony/sistema/scheduler/HorarioScheduler.java",
    "src/main/java/com/harmony/sistema/controller/publico/TallerRestController.java",
    "src/main/java/com/harmony/sistema/controller/publico/ProfesorPublicController.java",
    "src/main/java/com/harmony/sistema/controller/publico/IndexRestController.java",
    "src/main/java/com/harmony/sistema/controller/publico/ContactoController.java",
    "src/main/java/com/harmony/sistema/config/ApplicationConfig.java",
    "src/main/java/com/harmony/sistema/config/DataInitializer.java",
]

def fix_log_formatting(line):
    """Elimina espacios iniciales y estandariza formato de logs"""
    if "System.out.println" not in line and "System.err.println" not in line:
        return line
    
    # Patrones a reemplazar
    replacements = {
        # Emojis del scheduler
        r'🕒 \[SCHEDULER\]': '[INFO] [SCHEDULER]',
        r'✅ Horario ID': '[SUCCESS] [SCHEDULER] Horario ID',
        r'🔄 Total de': '[INFO] [SCHEDULER] Total de',
        r'ℹ️ No se encontraron': '[INFO] [SCHEDULER] No se encontraron',
        
        # Espacios iniciales en controllers
        r' \[REST REQUEST\]': '[INFO] [CONTROLLER]',
        r' \[REST SUCCESS\]': '[SUCCESS] [CONTROLLER]',
        r' \[REST\]': '[INFO] [CONTROLLER]',
        r' \[API REQUEST\]': '[INFO] [CONTROLLER]',
        r' \[API ADMIN\]': '[INFO] [CONTROLLER]',
        
        # Espacios iniciales en config
        r' \[CONFIG\]': '[INFO] [CONFIG]',
        r' \[DATA - SKIP\]': '[INFO] [DATA]',
        r'--- INICIANDO': '[INFO] [DATA] ========== INICIANDO',
        r'--- CONFIGURACIÓN INICIAL': '[INFO] [DATA] ========== CONFIGURACIÓN INICIAL',
        r'Profesores inicializados': '[INFO] [DATA] Profesores inicializados',
        r'Talleres inicializados': '[INFO] [DATA] Talleres inicializados',
    }
    
    modified = line
    for pattern, replacement in replacements.items():
        modified = re.sub(pattern, replacement, modified)
    
    # Cambiar a System.err.println para errores
    if "[ERROR]" in modified and "System.out.println" in modified:
        modified = modified.replace("System.out.println", "System.err.println")
    
    return modified

def process_file(file_path):
    """Procesa un archivo y corrige el formato de logs"""
    path = Path(file_path)
    if not path.exists():
        print(f"❌ Archivo no encontrado: {file_path}")
        return 0
    
    print(f"Procesando: {file_path}")
    
    try:
        with open(path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        modified_lines = []
        changes_count = 0
        
        for line in lines:
            new_line = fix_log_formatting(line)
            if new_line != line:
                changes_count += 1
            modified_lines.append(new_line)
        
        if changes_count > 0:
            with open(path, 'w', encoding='utf-8') as f:
                f.writelines(modified_lines)
            print(f"  ✓ {changes_count} líneas modificadas")
            return changes_count
        else:
            print(f"  - Sin cambios")
            return 0
            
    except Exception as e:
        print(f"  ✗ Error: {e}")
        return 0

def main():
    total_changes = 0
    
    for file_path in files_to_fix:
        changes = process_file(file_path)
        total_changes += changes
    
    print(f"\n{'='*50}")
    print(f"Total de líneas modificadas: {total_changes}")
    print(f"{'='*50}")

if __name__ == "__main__":
    main()
