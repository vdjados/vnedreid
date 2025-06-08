from flask import Flask, request, jsonify
import json
import numpy as np
import torch
from transformers import AutoTokenizer, AutoModel
import joblib
import re

app = Flask(__name__)

class ModelContainer:
    def __init__(self):
        print("\n=== Инициализация модели ===")
        # Загрузка BERT (используем более легкую версию)
        self.bert_model_name = "cointegrated/rubert-tiny"
        print(f"Загрузка BERT модели: {self.bert_model_name}")
        self.tokenizer = None
        self.bert_model = None
        
        # Загрузка ML модели и скейлера
        print("\nЗагрузка ML модели и скейлера...")
        self.ml_model = joblib.load('model.joblib')
        self.scaler = joblib.load('scaler.joblib')
        print(f"Количество признаков в скейлере: {self.scaler.n_features_in_}")
        print(f"Количество признаков в ML модели: {self.ml_model.n_features_in_ if hasattr(self.ml_model, 'n_features_in_') else 'неизвестно'}")
        
        # Спам слова
        self.spam_words = ["крипт", "заработ", "гарант", "дешев", "прибыл", 
                          "forex", "bitcoin", "акци", "бесплатн"]
        print(f"Количество спам-слов: {len(self.spam_words)}")
        print("Инициализация завершена!")

    def _load_bert_if_needed(self):
        if self.tokenizer is None or self.bert_model is None:
            print("Загрузка BERT модели...")
            self.tokenizer = AutoTokenizer.from_pretrained(self.bert_model_name)
            self.bert_model = AutoModel.from_pretrained(self.bert_model_name)
            print("BERT модель загружена!")

    def clean_text(self, text: str) -> str:
        if not isinstance(text, str):
            return ""
        text = text.lower()
        text = re.sub(r'https?://\S+|www\.\S+', '', text)
        text = re.sub(r'[\U00010000-\U0010ffff]', '', text, flags=re.UNICODE)
        text = re.sub(r'\s+', ' ', text).strip()
        return text

    def get_bert_embedding(self, text: str) -> np.ndarray:
        if not isinstance(text, str) or text.strip() == "":
            return np.zeros(312)

        self._load_bert_if_needed()
        
        inputs = self.tokenizer(text, return_tensors="pt", truncation=True, padding=True)
        with torch.no_grad():
            outputs = self.bert_model(**inputs)
        embedding = outputs.last_hidden_state.mean(dim=1).squeeze().numpy()
        print(f"Размер BERT эмбеддинга: {embedding.shape}")
        print(f"Среднее значение BERT эмбеддинга: {np.mean(embedding):.4f}")
        print(f"Стандартное отклонение BERT эмбеддинга: {np.std(embedding):.4f}")
        return embedding

    def normalize_features(self, features: np.ndarray) -> np.ndarray:
        # Нормализуем все признаки с помощью скейлера
        features_scaled = self.scaler.transform(features.reshape(1, -1))
        return features_scaled.flatten()


    def extract_features(self, text: str) -> np.ndarray:
        print("\n=== Извлечение признаков ===")
        cleaned_text = self.clean_text(text)
        print(f"Очищенный текст: {cleaned_text[:100]}...")
        
        # BERT эмбеддинг
        bert_embedding = self.get_bert_embedding(cleaned_text)
        
        # Дополнительные признаки
        has_link = 1 if re.search(r'https?://|www\.', text) else 0
        has_caps = 1 if any(word.isupper() and len(word) > 2 for word in text.split()) else 0
        mention_count = len(re.findall(r'@\w+', text))
        has_hashtag = 1 if re.search(r'#\w+', text) else 0
        
        spam_keyword_count = sum(1 for word in cleaned_text.split() 
                               if any(kw in word for kw in self.spam_words))
        
        excl_quest_count = sum(1 for c in cleaned_text if c in '!?')
        text_length = len(cleaned_text)
        word_count = len(cleaned_text.split())
        
        # Добавляем плотность пунктуации
        punctuation_density = excl_quest_count / text_length if text_length > 0 else 0
        
        # Выводим все признаки
        print("\nЗначения признаков:")
        print(f"has_link: {has_link}")
        print(f"has_caps: {has_caps}")
        print(f"mention_count: {mention_count}")
        print(f"has_hashtag: {has_hashtag}")
        print(f"spam_keyword_count: {spam_keyword_count}")
        print(f"excl_quest_count: {excl_quest_count}")
        print(f"text_length: {text_length}")
        print(f"word_count: {word_count}")
        print(f"punctuation_density: {punctuation_density}")
        
        # Объединяем все признаки
        additional_features = np.array([
            has_link, has_caps, mention_count, has_hashtag,
            spam_keyword_count, excl_quest_count, text_length, word_count,
            punctuation_density  # Добавляем новый признак
        ])
        print(f"Размер дополнительных признаков: {additional_features.shape}")
        
        combined_features = np.concatenate([bert_embedding, additional_features])
        print(f"Итоговый размер признаков: {combined_features.shape}")
        return combined_features

# Инициализация контейнера с моделями
print("\n=== Запуск сервера ===")
model_container = ModelContainer()



@app.route('/check_spam', methods=['POST'])
def check_spam():
    try:
        data = request.get_json()
        print("\n=== Получен новый запрос ===")
        print("Received message:", json.dumps(data, ensure_ascii=False, indent=2))
        
        text = data.get('text', '')
        username = data.get('username', '')
        
        # Извлекаем признаки
        features = model_container.extract_features(text)
        
        # Нормализуем признаки
        print("\n=== Нормализация признаков ===")
        print(f"Размер признаков до нормализации: {features.shape}")
        print(f"Среднее значение признаков до нормализации: {np.mean(features):.4f}")
        print(f"Стандартное отклонение признаков до нормализации: {np.std(features):.4f}")
        
        features_scaled = model_container.normalize_features(features)
        print(f"Размер признаков после нормализации: {features_scaled.shape}")
        print(f"Среднее значение признаков после нормализации: {np.mean(features_scaled):.4f}")
        print(f"Стандартное отклонение признаков после нормализации: {np.std(features_scaled):.4f}")
        
        # Получаем предсказание
        prediction = model_container.ml_model.predict(features_scaled.reshape(1, -1))[0]
        probabilities = model_container.ml_model.predict_proba(features_scaled.reshape(1, -1))[0]
        
        # Выводим вероятности в более читаемом формате
        print("\nВероятности классов:")
        print(f"Не спам: {probabilities[0]:.4%}")
        print(f"Спам: {probabilities[1]:.4%}")
        
        response = {
            "is_spam": bool(prediction),
            "confidence": float(probabilities[1])
        }
        
        print("\n=== Отправка ответа ===")
        print("Sending response:", json.dumps(response, ensure_ascii=False, indent=2))
        return jsonify(response)
        
    except Exception as e:
        print(f"\n=== Ошибка при обработке запроса ===")
        print(f"Error processing request: {str(e)}")
        return jsonify({
            "error": "Internal server error",
            "details": str(e)
        }), 500

if __name__ == '__main__':
    print("\n=== Запуск сервера ===")
    print("Starting ML-powered spam detection server on http://localhost:8000")
    print("Press Ctrl+C to stop")
    app.run(host='0.0.0.0', port=8000)
