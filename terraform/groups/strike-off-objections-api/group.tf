resource "aws_iam_group" "group" {
  name = "${var.service}-${var.environment}-encryption"
}

resource "aws_iam_group_membership" "membership" {
  group = aws_iam_group.group.name
  name  = "${var.service}-${var.environment}-encryption"
  users = [
    aws_iam_user.user.name
  ]
}
