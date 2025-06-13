package fcu.iecs.model;

public enum Type {
    EXPENSE {
        @Override
        public String toString() {
            return "支出";
        }
    }, INCOME {
        @Override
        public String toString() {
            return "收入";
        }
    }
}
