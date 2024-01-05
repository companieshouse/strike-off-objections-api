resource "aws_iam_group_policy" "group_policy" {
  name      = "${var.service}-${var.environment}-encryption"
  group     = aws_iam_group.group.name
  policy    = data.aws_iam_policy_document.policy.json
}
